package com.x.thread.producer;

import com.x.thread.atomic.AtomicCounter;
import com.x.thread.atomic.AtomicLatch;
import com.x.thread.function.Worker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class IntegerFuture extends WorkerFuture {

    protected IntegerFuture(RunCallable callable) {
        super(callable);
    }

    public static IntegerFuture create(Producer<Integer> producer, Worker<Integer> worker,
                                       final int start, final int end) {
        RunCallable callable = new RunCallable(producer.producerExecutor(), worker, producer.createObserver(), start, end);
        return new IntegerFuture(callable);
    }

    static class RunCallable extends WorkerFuture.RunCallable<Integer> {
        final int start;
        final int end;

        RunCallable(ThreadPoolExecutor executor, Worker<Integer> worker, ProducerObserver observer, int start, int end) {
            super(executor, worker, observer);
            this.start = start;
            this.end = end;
        }


        @Override
        protected AtomicLatch createLatch() {
            final int maxCount = start > end ? (start - end + 1) : (end - start + 1);
            return new Latch(new CountDownLatch(maxCount));
        }

        @Override
        protected AtomicCounter<Integer> createCounter(AtomicLatch latch) {
            if (start > end) {
                return new DecrementCounter(start, end, latch);
            } else {
                return new IncrementCounter(start, end, latch);
            }
        }
    }

    private static class DecrementCounter implements AtomicCounter<Integer> {
        final int end;
        final AtomicInteger atomic;
        final AtomicLatch latch;

        public DecrementCounter(int start, int end, AtomicLatch latch) {
            this.end = end;
            this.atomic = new AtomicInteger(start);
            this.latch = latch;
        }

        @Override
        public boolean checkCounter(Integer index) {
            return index >= end;
        }

        @Override
        public void countDown() {
            latch.countDown();
        }

        @Override
        public Integer getAndCounter() {
            return atomic.getAndDecrement();
        }
    }

    private static class IncrementCounter implements AtomicCounter<Integer> {
        final int end;
        final AtomicInteger atomic;
        final AtomicLatch latch;

        public IncrementCounter(int start, int end, AtomicLatch latch) {
            this.end = end;
            this.atomic = new AtomicInteger(start);
            this.latch = latch;
        }

        @Override
        public boolean checkCounter(Integer index) {
            return index <= end;
        }

        @Override
        public void countDown() {
            latch.countDown();
        }

        @Override
        public Integer getAndCounter() {
            return atomic.getAndIncrement();
        }
    }

    private static class Latch implements AtomicLatch {
        CountDownLatch latch;

        public Latch(CountDownLatch latch) {
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
