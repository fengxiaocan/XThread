package com.x.thread.producer;

import com.x.thread.atomic.AtomicCounter;
import com.x.thread.atomic.AtomicLatch;
import com.x.thread.atomic.LongCountDownLatch;
import com.x.thread.function.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class LongFuture extends WorkerFuture {

    protected LongFuture(WorkerFuture.RunCallable callable) {
        super(callable);
    }

    public static LongFuture create(Producer<Long> producer, Worker<Long> worker,
                                    final long start, final long end) {
        RunCallable callable = new RunCallable(producer.producerExecutor(), producer.getMaxCoreCount(), worker, producer.createObserver(), start, end);
        return new LongFuture(callable);
    }


    static class RunCallable extends WorkerFuture.RunCallable<Long> {
        final long start;
        final long end;

        RunCallable(ExecutorService executor, int maxCoreCount, Worker<Long> worker, ProducerObserver observer, long start, long end) {
            super(executor, maxCoreCount, worker, observer);
            this.start = start;
            this.end = end;
        }

        @Override
        protected AtomicLatch createLatch() {
            final long maxCount = start > end ? (start - end + 1) : (end - start + 1);
            return new Latch(new LongCountDownLatch(maxCount));
        }

        @Override
        protected AtomicCounter<Long> createCounter(AtomicLatch latch) {
            if (start > end) {
                return new LongDecrementCounter(start, end, latch);
            } else {
                return new LongIncrementCounter(start, end, latch);
            }
        }
    }

    private static class LongIncrementCounter implements AtomicCounter<Long> {
        final long end;
        final AtomicLong atomic;
        final AtomicLatch latch;

        public LongIncrementCounter(long start, long end, AtomicLatch latch) {
            this.end = end;
            this.atomic = new AtomicLong(start);
            this.latch = latch;
        }

        @Override
        public boolean checkCounter(Long index) {
            return index <= end;
        }

        @Override
        public void countDown() {
            latch.countDown();
        }

        @Override
        public Long getAndCounter() {
            return atomic.getAndIncrement();
        }
    }

    /**
     * 自减
     */
    private static class LongDecrementCounter implements AtomicCounter<Long> {
        final long end;
        final AtomicLong atomic;
        final AtomicLatch latch;

        public LongDecrementCounter(long start, long end, AtomicLatch latch) {
            this.end = end;
            this.atomic = new AtomicLong(start);
            this.latch = latch;
        }

        @Override
        public boolean checkCounter(Long index) {
            return index >= end;
        }

        @Override
        public void countDown() {
            latch.countDown();
        }

        @Override
        public Long getAndCounter() {
            return atomic.getAndDecrement();
        }
    }

    private static class Latch implements AtomicLatch {
        LongCountDownLatch latch;

        public Latch(LongCountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void await() throws InterruptedException {
            latch.await();
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        @Override
        public void countDown() {
            latch.countDown();
        }
    }
}
