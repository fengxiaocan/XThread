package com.x.thread.provide;

import com.x.thread.execute.BAKThreadPoolExecutor;
import com.x.thread.execute.DequeThreadPoolExecutor;
import com.x.thread.execute.PriorityThreadPoolExecutor;
import com.x.thread.execute.RxExecutors;
import com.x.thread.function.RxFuture;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorProvide implements ExecutorSource {
    private final AtomicInteger atomic = new AtomicInteger(0);
    private RxExecutors rxExecutors;
    private ExecutorService coreExecutor;
    private int maxCoreCount = 5;
    private long keepAliveTime = 10000;
    private boolean allowCoreThreadTimeOut = true;
    private RxExecutors.Mode mode = RxExecutors.Mode.Priority;
    private Observer observer;

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
        if (rxExecutors != null) {
            rxExecutors.setCorePoolSize(maxCoreCount);
        }
        return this;
    }

    public final ExecutorProvide executeOn(ExecutorService poolExecutor) {
        this.coreExecutor = poolExecutor;
        return this;
    }

    public final ExecutorProvide executeOn(RxExecutors rxExecutors) {
        this.rxExecutors = rxExecutors;
        this.coreExecutor = null;
        return this;
    }

    public final ExecutorProvide subscribeOn(Observer observer) {
        this.observer = observer;
        return this;
    }

    public ExecutorProvide setKeepAliveTime(long keepAliveTime) {
        if (keepAliveTime >= 0) {
            this.keepAliveTime = keepAliveTime;
        }
        return this;
    }

    public ExecutorProvide setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
        long aliveTime = unit.toMillis(keepAliveTime);
        if (aliveTime >= 0) {
            this.keepAliveTime = aliveTime;
        }
        return this;
    }

    public final ExecutorProvide mode(RxExecutors.Mode mode) {
        this.mode = mode;
        if (rxExecutors != null) {
            rxExecutors.setMode(mode);
        }
        return this;
    }

    public final ExecutorProvide work() {
        rxExecutors = RxExecutors.Work(mode, maxCoreCount);
        return this;
    }

    public final ExecutorProvide single() {
        rxExecutors = RxExecutors.Single(mode);
        return this;
    }

    public final ExecutorProvide io() {
        rxExecutors = RxExecutors.IO(mode, maxCoreCount);
        return this;
    }

    public final ExecutorProvide newThread() {
        coreExecutor = RxExecutors.newThread(mode, maxCoreCount);
        return this;
    }

    public final ExecutorProvide allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public final List<Runnable> shutdownNow() {
        if (coreExecutor != null && !coreExecutor.isShutdown() && !coreExecutor.isTerminated()) {
            return coreExecutor.shutdownNow();
        }
        return null;
    }


    public final void shutdown() {
        if (coreExecutor != null && !coreExecutor.isShutdown() && !coreExecutor.isTerminated()) {
            coreExecutor.shutdown();
        }
    }

    public final boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        if (coreExecutor != null) {
            return coreExecutor.awaitTermination(time, unit);
        }
        return false;
    }

    public final boolean isShutdown() {
        if (coreExecutor != null) {
            return coreExecutor.isShutdown();
        }
        return true;
    }

    public final boolean isTerminated() {
        if (coreExecutor != null) {
            return coreExecutor.isTerminated();
        }
        return true;
    }

    public final ExecutorProvide rebuildExecutors() {
        synchronized (Object.class) {
            rxExecutors = null;
            initRxExecutors();
            rxExecutors.setKeepAliveTime(keepAliveTime);
            rxExecutors.setCorePoolSize(maxCoreCount);
            rxExecutors.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            coreExecutor = rxExecutors.executor();
        }
        return this;
    }

    protected final ExecutorService coreExecutor() {
        synchronized (Object.class) {
            if (coreExecutor == null || coreExecutor.isShutdown() || coreExecutor.isTerminated()) {
                initRxExecutors();
                coreExecutor = rxExecutors.executor();
            }
            if (coreExecutor instanceof BAKThreadPoolExecutor) {
                ((BAKThreadPoolExecutor) coreExecutor).setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
                ((BAKThreadPoolExecutor) coreExecutor).setCorePoolSize(maxCoreCount);
                ((BAKThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            } else if (coreExecutor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) coreExecutor).setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
                ((ThreadPoolExecutor) coreExecutor).setCorePoolSize(maxCoreCount);
                ((ThreadPoolExecutor) coreExecutor).allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            }
        }
        return coreExecutor;
    }

    private void initRxExecutors() {
        if (rxExecutors == null) {
            if (mode == RxExecutors.Mode.Priority) {
                rxExecutors = RxExecutors.createPriority("Work", maxCoreCount);
            } else if (mode == RxExecutors.Mode.Deque) {
                rxExecutors = RxExecutors.createDeque("Work", maxCoreCount);
            } else {
                rxExecutors = RxExecutors.createQueue("Work", maxCoreCount);
            }
        }
    }

    private Runnable observer(Runnable runnable) {
        if (observer != null) {
            return new ObserverRunnable(runnable, atomic, observer);
        } else {
            return runnable;
        }
    }

    private <V> Callable<V> observer(Callable<V> runnable) {
        if (observer != null) {
            return new ObserverCallback<V>(runnable, atomic, observer);
        } else {
            return runnable;
        }
    }

    @Override
    public <T> RxFuture<T> submit(Callable<T> var1) {
        ExecutorService service = coreExecutor();
        Future<T> submit = service.submit(observer(var1));
        return new ExecutorFuture<T>(submit);
    }

    @Override
    public <T> RxFuture<T> submit(Runnable var1, T var2) {
        ExecutorService service = coreExecutor();
        Future<T> submit = service.submit(observer(var1), var2);
        return new ExecutorFuture<T>(submit);
    }

    @Override
    public RxFuture<?> submit(Runnable var1) {
        ExecutorService service = coreExecutor();
        Future<?> submit = service.submit(observer(var1));
        return new ExecutorFuture(submit);
    }

    @Override
    public void execute(Runnable var1) {
        coreExecutor().execute(observer(var1));
    }

    @Override
    public <T> RxFuture<T> prioritySubmit(Callable<T> var1) {
        ExecutorService service = coreExecutor();
        Future<T> submit;
        if (service instanceof PriorityThreadPoolExecutor) {
            submit = ((PriorityThreadPoolExecutor) service).submit(observer(var1), true);
        } else if (service instanceof DequeThreadPoolExecutor) {
            submit = ((DequeThreadPoolExecutor) service).submit(observer(var1), true);
        } else {
            submit = service.submit(observer(var1));
        }
        return new ExecutorFuture<T>(submit);
    }

    @Override
    public <T> RxFuture<T> prioritySubmit(Runnable var1, T var2) {
        ExecutorService service = coreExecutor();
        Future<T> submit;
        if (service instanceof PriorityThreadPoolExecutor) {
            submit = ((PriorityThreadPoolExecutor) service).submit(observer(var1), var2, true);
        } else if (service instanceof DequeThreadPoolExecutor) {
            submit = ((DequeThreadPoolExecutor) service).submit(observer(var1), var2, true);
        } else {
            submit = service.submit(observer(var1), var2);
        }
        return new ExecutorFuture(submit);
    }

    @Override
    public RxFuture<?> prioritySubmit(Runnable var1) {
        ExecutorService service = coreExecutor();
        Future<?> submit;
        if (service instanceof PriorityThreadPoolExecutor) {
            submit = ((PriorityThreadPoolExecutor) service).submit(observer(var1), true);
        } else if (service instanceof DequeThreadPoolExecutor) {
            submit = ((DequeThreadPoolExecutor) service).submit(observer(var1), true);
        } else {
            submit = service.submit(observer(var1));
        }
        return new ExecutorFuture(submit);
    }

    @Override
    public void priorityExecute(Runnable var1) {
        ExecutorService executor = coreExecutor();
        if (executor instanceof PriorityThreadPoolExecutor) {
            ((PriorityThreadPoolExecutor) executor).execute(observer(var1), true);
        } else if (executor instanceof DequeThreadPoolExecutor) {
            ((DequeThreadPoolExecutor) executor).execute(observer(var1), true);
        } else {
            executor.execute(observer(var1));
        }
    }
}
