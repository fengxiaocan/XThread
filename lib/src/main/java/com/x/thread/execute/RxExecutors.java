package com.x.thread.execute;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class RxExecutors extends CacheExecutors {
    private BAKThreadPoolExecutor executor;
    private int sCoreExecutorCount;
    private long keepAliveTime = 10000;
    private boolean allowCoreThreadTimeOut = true;
    private Mode mode = Mode.Queue;
    private ThreadFactory threadFactory;
    private BAKThreadPoolExecutor.RejectedExecutionHandler handler;

    private RxExecutors(String name, final int threadCount) {
        this.sCoreExecutorCount = threadCount;
        this.threadFactory = defaultThreadFactory(name);
    }

    private RxExecutors(String name, final int threadCount, Mode mode) {
        this.sCoreExecutorCount = threadCount;
        this.threadFactory = defaultThreadFactory(name);
        this.mode = mode;
    }

    public static RxExecutors getExecutor(String name, int threadCount, RxExecutors.Mode mode) {
        synchronized (Object.class) {
            String key = mode.prefixName(name);
            RxExecutors pool = getPool(key);
            if (pool == null) {
                pool = new RxExecutors(key, threadCount, mode);
                cache.put(key, pool);
            }
            pool.setCorePoolSize(threadCount);
            return pool;
        }
    }

    public static ExecutorService newQueueExecutor(int threadSize) {
        return new QueueThreadPoolExecutor(threadSize, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService newQueueExecutor() {
        return new QueueThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService newDequeExecutor(int threadSize) {
        return new DequeThreadPoolExecutor(threadSize, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService newDequeExecutor() {
        return new DequeThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService newPriorityExecutor(int threadSize) {
        return new PriorityThreadPoolExecutor(threadSize, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ExecutorService newPriorityExecutor() {
        return new PriorityThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS);
    }

    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    public static ThreadFactory defaultThreadFactory(String name) {
        return new DefaultThreadFactory(name);
    }

    public static ThreadFactory defaultThreadFactory(String name, int priority, boolean isDaemon) {
        return new DefaultThreadFactory(name, priority, isDaemon);
    }

    public static RxExecutors createQueue(String name, int threadCount) {
        return Queue().create(name, threadCount);
    }

    public static RxExecutors createDeque(String name, int threadCount) {
        return Deque().create(name, threadCount);
    }

    public static RxExecutors createPriority(String name, int threadCount) {
        return Priority().create(name, threadCount);
    }

    public static RxExecutors create(String name, int threadCount, Mode mode) {
        return mode.create(name, threadCount);
    }

    public static Mode Queue() {
        return Mode.Queue;
    }

    public static Mode Deque() {
        return Mode.Deque;
    }

    public static Mode Priority() {
        return Mode.Priority;
    }

    public synchronized RxExecutors setCorePoolSize(int threadCount) {
        if (threadCount > 0) {
            sCoreExecutorCount = Math.max(1, threadCount);
            if (checkState()) {
                executor.setCorePoolSize(threadCount);
            }
        }
        return this;
    }

    public synchronized RxExecutors setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
        }
        return this;
    }

    public synchronized RxExecutors setKeepAliveTime(long keepAliveTime) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime;
            if (checkState()) {
                executor.setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
            }
        }
        return this;
    }

    public synchronized RxExecutors setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = unit.toMillis(keepAliveTime);
            if (checkState()) {
                executor.setKeepAliveTime(keepAliveTime, unit);
            }
        }
        return this;
    }

    public synchronized RxExecutors setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory != null) {
            this.threadFactory = threadFactory;
            if (checkState()) {
                executor.setThreadFactory(threadFactory);
            }
        }
        return this;
    }

    public synchronized RxExecutors setRejectedExecutionHandler(BAKThreadPoolExecutor.RejectedExecutionHandler handler) {
        if (handler != null) {
            this.handler = handler;
            if (checkState()) {
                executor.setRejectedExecutionHandler(handler);
            }
        }
        return this;
    }

    private boolean checkState() {
        if (executor == null) {
            return false;
        }
        if (executor.isShutdown()) {
            return false;
        }
        return !executor.isTerminated();
    }

    public synchronized RxExecutors allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        if (checkState()) {
            executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        }
        return this;
    }

    public synchronized RxExecutors shutdown() {
        if (checkState()) {
            executor.shutdown();
        }
        return this;
    }

    public synchronized List<Runnable> shutdownNow() {
        if (checkState()) {
            return executor.shutdownNow();
        }
        return null;
    }

    public synchronized RxExecutors recycler() {
        if (checkState()) {
            executor.shutdownNow();
        }
        executor = null;
        return this;
    }

    private BAKThreadPoolExecutor createExecutor() {
        switch (mode) {
            default:
            case Queue: {
                if (handler != null) {
                    return new QueueThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory, handler);
                } else {
                    return new QueueThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory);
                }
            }
            case Deque:
                if (handler != null) {
                    return new DequeThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory, handler);
                } else {
                    return new DequeThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory);
                }
            case Priority:
                if (handler != null) {
                    return new PriorityThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory, handler);
                } else {
                    return new PriorityThreadPoolExecutor(sCoreExecutorCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.MILLISECONDS, threadFactory);
                }
        }
    }

    public BAKThreadPoolExecutor executor() {
        synchronized (Object.class) {
            if (executor == null || executor.isShutdown() || executor.isTerminated()) {
                synchronized (Object.class) {
                    executor = createExecutor();
                    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
                }
            }
            return executor;
        }
    }

    public enum Mode {
        Queue("Queue"), Deque("Deque"), Priority("Priority");
        String name;
        String preName;

        Mode(String name) {
            this.name = name;
            this.preName = name + "-";
        }

        public String prefixName(String name) {
            return this.preName + name;
        }

        public String getName() {
            return name;
        }

        public RxExecutors create(String name, int threadCount) {
            return new RxExecutors(prefixName(name), threadCount, this);
        }
    }
}
