package com.x.thread.producer;

import com.x.thread.execute.RxExecutors;
import com.x.thread.function.Observer;
import com.x.thread.scheduler.Scheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public abstract class Producer<T> implements ProducerSource<T> {
    private RxExecutors rxExecutors;
    private ThreadFactory threadFactory;
    private ExecutorService coreExecutor;
    private int maxCoreCount = 5;
    private Observer observer;
    private Scheduler schedulers;
    private RxExecutors.Mode mode = RxExecutors.Priority();

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

    public final Producer<T> executeOn(RxExecutors executor) {
        this.rxExecutors = executor;
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

    public final Producer<T> mode(RxExecutors.Mode mode) {
        this.mode = mode;
        if (rxExecutors != null) {
            rxExecutors.setMode(mode);
        }
        coreExecutor = null;
        return this;
    }

    public final Producer<T> work() {
        rxExecutors = RxExecutors.Work(mode);
        coreExecutor = null;
        return this;
    }

    public final Producer<T> work(int threadCount) {
        rxExecutors = RxExecutors.Work(mode, threadCount);
        coreExecutor = null;
        return this;
    }

    public final Producer<T> single() {
        rxExecutors = RxExecutors.Single(mode);
        coreExecutor = null;
        return this;
    }

    public final Producer<T> io() {
        rxExecutors = RxExecutors.IO(mode);
        coreExecutor = null;
        return this;
    }

    public final Producer<T> io(int threadCount) {
        rxExecutors = RxExecutors.IO(mode, threadCount);
        coreExecutor = null;
        return this;
    }

    public final Producer<T> newThread() {
        coreExecutor = RxExecutors.newThread(mode, 100);
        return this;
    }

    public final Producer<T> newThread(int threadCount) {
        coreExecutor = RxExecutors.newThread(mode, threadCount);
        return this;
    }

    protected final int getMaxCoreCount() {
        return maxCoreCount;
    }

    protected final ProducerObserver createObserver() {
        return new ProducerObserver(observer, schedulers);
    }

    protected final ExecutorService coreExecutor() {
        synchronized (Object.class) {
            if (coreExecutor == null || coreExecutor.isShutdown() || coreExecutor.isTerminated()) {
                if (rxExecutors == null) {
                    rxExecutors = defaultExecutors();
                }
                coreExecutor = rxExecutors.executor();
            }
        }
        return coreExecutor;
    }

    private RxExecutors defaultExecutors() {
        return RxExecutors.Work(mode);
    }

    protected final ExecutorService producerExecutor() {
        RxExecutors executors = RxExecutors.createQueue("Work", maxCoreCount);
        if (threadFactory != null) {
            executors.setThreadFactory(threadFactory);
        }
        return executors.executor();
    }
}
