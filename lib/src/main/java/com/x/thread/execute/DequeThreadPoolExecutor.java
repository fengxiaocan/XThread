package com.x.thread.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DequeThreadPoolExecutor extends BAKThreadPoolExecutor {
    public DequeThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS);
    }

    public DequeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<>());
    }

    public DequeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<>(), threadFactory);
    }

    public DequeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<>(), handler);
    }

    public DequeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<>(), threadFactory, handler);
    }

    public Future<?> submit(Runnable task, boolean isUrgent) {
        if (task == null) {
            throw new NullPointerException();
        } else {
            RunnableFuture<?> ftask = newTaskFor(task, null);
            this.execute(ftask, isUrgent);
            return ftask;
        }
    }

    public <T> Future<T> submit(Runnable task, T result, boolean isUrgent) {
        if (task == null) {
            throw new NullPointerException();
        } else {
            RunnableFuture<T> ftask = newTaskFor(task, result);
            this.execute(ftask, isUrgent);
            return ftask;
        }
    }

    public <T> Future<T> submit(Callable<T> task, boolean isUrgent) {
        if (task == null) {
            throw new NullPointerException();
        } else {
            RunnableFuture<T> ftask = newTaskFor(task);
            this.execute(ftask, isUrgent);
            return ftask;
        }
    }

    public void execute(Runnable command, boolean isUrgent) {
        if (isUrgent) {
            if (command == null) {
                throw new NullPointerException();
            } else {
                int c = this.ctl.get();
                if (workerCountOf(c) < this.corePoolSize) {
                    if (this.addWorker(command, true)) {
                        return;
                    }

                    c = this.ctl.get();
                }

                if (isRunning(c) && ((LinkedBlockingDeque) this.workQueue).offerFirst(command)) {
                    int recheck = this.ctl.get();
                    if (!isRunning(recheck) && this.remove(command)) {
                        this.reject(command);
                    } else if (workerCountOf(recheck) == 0) {
                        this.addWorker(null, false);
                    }
                } else if (!this.addWorker(command, false)) {
                    this.reject(command);
                }

            }
        } else {
            super.execute(command);
        }
    }
}
