package com.x.thread.producer;

import com.x.thread.atomic.AtomicCounter;
import com.x.thread.atomic.AtomicLatch;
import com.x.thread.function.Cancelled;
import com.x.thread.function.Worker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

abstract class WorkerFuture extends FutureTask<Long> {
    private final RunCallable runCallable;

    protected WorkerFuture(RunCallable callable) {
        super(callable);
        runCallable = callable;
    }

    @Override
    protected void done() {
        runCallable.done();
        super.done();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        runCallable.cancel(mayInterruptIfRunning);
        return super.cancel(mayInterruptIfRunning);
    }

    static abstract class RunCallable<T> implements Callable<Long> {
        private final long startTime;
        private final Worker<T> worker;
        private final ProducerObserver observer;
        private final ThreadPoolExecutor executor;

        private final List<Future<?>> futures;
        private final List<Cancelled> canceledList;

        RunCallable(ThreadPoolExecutor executor, Worker<T> worker,
                    ProducerObserver observer) {
            this.executor = executor;
            this.worker = worker;
            this.observer = observer;
            startTime = System.currentTimeMillis();
            futures = new LinkedList<>();
            canceledList = new LinkedList<>();
        }

        protected abstract AtomicLatch createLatch();

        protected abstract AtomicCounter<T> createCounter(AtomicLatch latch);

        @Override
        public final Long call() throws Exception {
            final AtomicLatch latch = createLatch();
            final AtomicCounter<T> counter = createCounter(latch);

            final int maxCoreCount = executor.getCorePoolSize();
            for (int i = 0; i < maxCoreCount; i++) {
                ProducerRunnable task = new ProducerRunnable(worker, observer, counter);
                Future<?> future = executor.submit(task);
                futures.add(future);
                canceledList.add(task);
            }
            try {
                latch.await();
                executor.shutdown();
                observer.onComplete();
            } catch (InterruptedException e) {
                //取消
                executor.shutdown();
                observer.onCancel();
            } catch (Throwable e) {
                observer.onError(e);
                cancel(true);
                executor.shutdownNow();
                observer.onCancel();
            }
            return System.currentTimeMillis() - startTime;
        }

        private final void done() {
            futures.clear();
            canceledList.clear();
        }

        private final void cancel(boolean mayInterruptIfRunning) {
            for (Cancelled future : canceledList) {
                future.cancel();
            }
            for (Future future : futures) {
                future.cancel(mayInterruptIfRunning);
            }
            canceledList.clear();
            futures.clear();
        }

    }
}
