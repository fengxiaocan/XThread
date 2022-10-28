package com.x.thread.execute;

import com.x.thread.RxThreadPool;
import com.x.thread.function.RxFuture;
import com.x.thread.thread.BinaryThreadPoolExecutor;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorProvide implements ExecutorSource {
    private ExecutorService coreExecutor;
    private int maxCoreCount = 5;
    private long keepAliveTime = 10000;
    //1：在没有其他程序运行的情况下运行。2-3：后台计算；4-6：IO；7-9：交互，事件驱动；10：关键问题；
    private int priority = 5;
    private boolean isDaemon = false;
    private String name = "Core-Work";
    private boolean allowCoreThreadTimeOut = true;

    public ExecutorProvide() {
    }

    public ExecutorProvide(int maxCoreCount) {
        this.maxCoreCount = Math.max(1, maxCoreCount);
    }

    public static ExecutorProvide create(final int maxCoreCount) {
        return new ExecutorProvide(maxCoreCount);
    }

    public final ExecutorProvide executeCore(int maxCoreCount) {
        this.maxCoreCount = Math.max(1, maxCoreCount);
        return this;
    }

    public final ExecutorProvide executeOn(ExecutorService poolExecutor) {
        this.coreExecutor = poolExecutor;
        return this;
    }

    public final ExecutorProvide setPriority(int priority) {
        this.priority = Math.min(Math.max(priority, 1), 10);
        return this;
    }

    public final ExecutorProvide calculate() {
        this.priority = 1;
        return this;
    }

    public final ExecutorProvide setDaemon(boolean daemon) {
        isDaemon = daemon;
        return this;
    }
    public final ExecutorProvide setWorkName(String name) {
        if (name != null && !name.equals("")) {
            this.name = name;
        }
        return this;
    }

    public ExecutorProvide setKeepAliveTime(long keepAliveTime) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime;
        }
        return this;
    }
    public ExecutorProvide setKeepAliveTime(long keepAliveTime,TimeUnit unit) {
        long aliveTime = unit.toMillis(keepAliveTime);
        if (aliveTime>0) {
            this.keepAliveTime = aliveTime;
        }
        return this;
    }

    public final ExecutorProvide work() {
        coreExecutor = RxThreadPool.work().executor();
        ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        name = "Core-Work";
        priority = 5;
        return this;
    }

    public final ExecutorProvide single() {
        coreExecutor = RxThreadPool.single().executor();
        ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        name = "Single-Work";
        priority = 5;
        return this;
    }

    public final ExecutorProvide io() {
        coreExecutor = RxThreadPool.io().executor();
        ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        name = "Io-Work";
        priority = 7;
        return this;
    }

    public final ExecutorProvide newThread() {
        coreExecutor = RxThreadPool.newThread();
        ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        name = "New-Work";
        priority = 5;
        return this;
    }

    public final ExecutorProvide allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        if (coreExecutor != null) {
            if (coreExecutor instanceof BinaryThreadPoolExecutor) {
                ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            }else if (coreExecutor instanceof ThreadPoolExecutor){
                ((ThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            }
        }
        return this;
    }

    public final List<Runnable> shutdownNow(){
        if (coreExecutor != null && !coreExecutor.isShutdown() && !coreExecutor.isTerminated()) {
            return coreExecutor.shutdownNow();
        }
        return null;
    }

    public final void shutdown(){
        if (coreExecutor != null && !coreExecutor.isShutdown() && !coreExecutor.isTerminated()) {
            coreExecutor.shutdown();
        }
    }
    public final boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        if (coreExecutor != null) {
            return coreExecutor.awaitTermination(time,unit);
        }
        return false;
    }

    public final boolean isShutdown(){
        if (coreExecutor != null) {
            return coreExecutor.isShutdown();
        }
        return true;
    }

    public final boolean isTerminated(){
        if (coreExecutor != null) {
            return coreExecutor.isTerminated();
        }
        return true;
    }

    public final ExecutorProvide rebuildExecutorService(){
        coreExecutor = RxThreadPool.createExecutor(name, maxCoreCount, keepAliveTime, TimeUnit.MILLISECONDS, priority, isDaemon);
        ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return this;
    }

    protected final ExecutorService coreExecutor() {
        if (coreExecutor == null || coreExecutor.isShutdown() || coreExecutor.isTerminated()) {
            coreExecutor = RxThreadPool.createExecutor(name, maxCoreCount, keepAliveTime, TimeUnit.MILLISECONDS, priority, isDaemon);
            ((BinaryThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        }
        return coreExecutor;
    }


    @Override
    public <T> RxFuture<T> submit(Callable<T> var1) {
        return new ExecutorFuture(coreExecutor().submit(var1));
    }

    @Override
    public <T> RxFuture<T> submit(Runnable var1, T var2) {
        return new ExecutorFuture(coreExecutor().submit(var1, var2));
    }

    @Override
    public RxFuture<?> submit(Runnable var1) {
        return new ExecutorFuture(coreExecutor().submit(var1));
    }

    @Override
    public void execute(Runnable var1) {
        coreExecutor().execute(var1);
    }

    @Override
    public <T> RxFuture<T> submit(Callable<T> var1, boolean isCore) {
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor){
            return new ExecutorFuture(((BinaryThreadPoolExecutor)executor).submit(var1,isCore));
        } else {
            return new ExecutorFuture(executor.submit(var1));
        }
    }

    @Override
    public <T> RxFuture<T> submit(Runnable var1, T var2, boolean isCore) {
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor) {
            return new ExecutorFuture(((BinaryThreadPoolExecutor) executor).submit(var1, var2, isCore));
        } else {
            return new ExecutorFuture(executor.submit(var1, var2));
        }
    }

    @Override
    public RxFuture<?> submit(Runnable var1, boolean isCore) {
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor) {
            return new ExecutorFuture(((BinaryThreadPoolExecutor) executor).submit(var1, isCore));
        } else {
            return new ExecutorFuture(executor.submit(var1));
        }
    }

    @Override
    public void execute(Runnable var1, boolean isCore) {
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor) {
            ( (BinaryThreadPoolExecutor) executor).execute(var1, isCore);
        }else {
            executor.execute(var1);
        }
    }
}
