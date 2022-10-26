package com.x.thread;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class RxThreadPool {
    private static Pool corePool;
    private static Pool ioPool;
    private static Pool singlePool;

    public static ThreadPoolExecutor createExecutor(int maxCount, long keepAliveTime, TimeUnit unit) {
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(maxCount,
                Integer.MAX_VALUE, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory());
        return taskExecutor;
    }

    public static ThreadPoolExecutor createExecutor(String name, int maxCount, long keepAliveTime, TimeUnit unit) {
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(maxCount,
                Integer.MAX_VALUE, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(name));
        return taskExecutor;
    }


    public static ThreadPoolExecutor createExecutor(String name, int maxCount, long keepAliveTime, TimeUnit unit, int priority, boolean isDaemon) {
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(maxCount,
                Integer.MAX_VALUE, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(name, priority,isDaemon));
        return taskExecutor;
    }

    public static ThreadPoolExecutor newThread(long keepAliveTime, TimeUnit unit) {
        return RxThreadPool.createExecutor(1000, keepAliveTime, unit);
    }

    public static ThreadPoolExecutor newThread() {
        return newThread(60, TimeUnit.SECONDS);
    }

    public static Pool work() {
        synchronized (RxThreadPool.class) {
            if (corePool == null) {
                corePool = new Pool("Core", 5);
            }
        }
        return corePool;
    }

    public static Pool single() {
        synchronized (RxThreadPool.class) {
            if (singlePool == null) {
                singlePool = new Pool("Single", 1);
            }
        }
        return singlePool;
    }

    public static Pool io() {
        synchronized (RxThreadPool.class) {
            if (ioPool == null) {
                ioPool = new Pool("Io", 2);
            }
        }
        return ioPool;
    }

    public static void recycler() {
        if (ioPool != null) {
            ioPool.recycler();
        }
        if (corePool != null) {
            corePool.recycler();
        }
        if (singlePool != null) {
            singlePool.recycler();
        }
    }

    public static final class Pool {
        private ThreadPoolExecutor executor;
        private final String name;
        private int sCoreExecutorCount;
        private long keepAliveTime = TimeUnit.SECONDS.toMillis(60);
        private boolean allowCoreThreadTimeOut = true;

        private Pool(String name, int count) {
            this.name = name;
            this.sCoreExecutorCount = count;
        }

        public void setCorePoolSize(int corePoolSize) {
            if (corePoolSize > 0) {
                sCoreExecutorCount = corePoolSize;
                if (checkPool()) {
                    executor.setCorePoolSize(corePoolSize);
                }
            }
        }

        public void setKeepAliveTime(long keepAliveTime) {
            if (keepAliveTime > 0) {
                this.keepAliveTime = keepAliveTime;
                if (checkPool()) {
                    executor.setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
                }
            }
        }

        public void setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
            if (keepAliveTime > 0) {
                this.keepAliveTime = unit.toMillis(keepAliveTime);
                if (checkPool()) {
                    executor.setKeepAliveTime(keepAliveTime, unit);
                }
            }
        }

        public void setThreadFactory(ThreadFactory threadFactory) {
            executor().setThreadFactory(threadFactory);
        }

        private boolean checkPool() {
            return executor != null && !executor.isShutdown() && !executor.isTerminated();
        }

        public void allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
            this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
            if (checkPool()) {
                executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
            }
        }

        public void shutdown() {
            if (checkPool()) {
                executor.shutdown();
            }
        }

        public List<Runnable> shutdownNow() {
            if (checkPool()) {
                return executor.shutdownNow();
            }
            return null;
        }

        public void recycler() {
            if (checkPool()) {
                executor.shutdownNow();
            }
            executor = null;
        }

        public ThreadPoolExecutor executor() {
            synchronized (ThreadPoolExecutor.class) {
                if (executor == null || executor.isShutdown() || executor.isTerminated()) {
                    synchronized (ThreadPoolExecutor.class) {
                        executor = createExecutor(name, sCoreExecutorCount, keepAliveTime, TimeUnit.MILLISECONDS);
                        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
                    }
                }
                return executor;
            }
        }
    }
}
