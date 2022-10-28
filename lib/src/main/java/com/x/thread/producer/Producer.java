package com.x.thread.producer;

import com.x.thread.RxThreadPool;
import com.x.thread.function.Observer;
import com.x.thread.scheduler.Scheduler;
import com.x.thread.thread.BinaryThreadPoolExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class Producer<T> implements ProducerSource<T> {
    private ThreadFactory threadFactory;
    private ExecutorService coreExecutor;
    private int maxCoreCount = 5;
    //1：在没有其他程序运行的情况下运行。2-3：后台计算；4-6：IO；7-9：交互，事件驱动；10：关键问题；
    private int priority = 5;
    private boolean isDaemon = false;
    private boolean isCore = false;
    private Observer observer;
    private Scheduler schedulers;
    private String name = "Core-Work";

    public static <T> Producer<T> from(T... array) {
        return new ArrayProducer<>(array);
    }

    public static <T> Producer<T> from(List<T> list) {
        return new ListProducer<>(list);
    }

    public static Producer<Integer> range(final int start, int end) {
        return new RangeProducer(start, end);
    }

    public static Producer<Long> range(final long start, long end) {
        return new RangeLongProducer(start, end);
    }

    public static Producer<Integer> create(int taskCount) {
        return new RangeProducer(1, taskCount);
    }

    public final Producer<T> executeCore(int maxCoreCount) {
        this.maxCoreCount = Math.max(1, maxCoreCount);
        return this;
    }

    public final Producer<T> threadBy(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public final Producer<T> executeOn(ExecutorService poolExecutor) {
        this.coreExecutor = poolExecutor;
        return this;
    }

    public final Producer<T> subscribeOn(Scheduler schedulers) {
        this.schedulers = schedulers;
        return this;
    }

    public final Producer<T> subscribe(Observer observer) {
        this.observer = observer;
        return this;
    }

    public final Producer<T> setPriority(int priority) {
        this.priority = Math.min(Math.max(priority, 1), 10);
        return this;
    }

    public final Producer<T> calculate() {
        this.priority = 6;
        return this;
    }

    public final Producer<T> setDaemon(boolean daemon) {
        isDaemon = daemon;
        return this;
    }
    public final Producer<T> setCore(boolean core) {
        isCore = core;
        return this;
    }

    public final Producer<T> single() {
        coreExecutor = RxThreadPool.single().executor();
        name = "Single-Work";
        priority = 5;
        return this;
    }

    public final Producer<T> io() {
        coreExecutor = RxThreadPool.io().executor();
        name = "Io-Work";
        priority = 7;
        return this;
    }

    public final Producer<T> newThread() {
        coreExecutor = RxThreadPool.newThread();
        name = "New-Work";
        priority = 5;
        return this;
    }

    protected final int getMaxCoreCount() {
        return maxCoreCount;
    }

    protected final boolean isCore() {
        return isCore;
    }

    protected final ProducerObserver createObserver() {
        return new ProducerObserver(observer, schedulers);
    }

    protected final ExecutorService coreExecutor() {
        if (coreExecutor == null || coreExecutor.isShutdown() || coreExecutor.isTerminated()) {
            coreExecutor = RxThreadPool.work().executor();
        }
        return coreExecutor;
    }

    protected final BinaryThreadPoolExecutor producerExecutor() {
        BinaryThreadPoolExecutor executor = RxThreadPool.createExecutor(name, maxCoreCount, 5, TimeUnit.SECONDS, priority, isDaemon);
        executor.allowCoreThreadTimeOut(true);
        if (threadFactory != null) {
            executor.setThreadFactory(threadFactory);
        }
        return executor;
    }
}
