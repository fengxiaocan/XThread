package com.x.thread.producer;

import com.x.thread.atomic.ArrayData;
import com.x.thread.atomic.AtomicCounter;
import com.x.thread.atomic.AtomicLatch;
import com.x.thread.function.Worker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class ArrayFuture<T> extends WorkerFuture {
    protected ArrayFuture(RunCallable<T> callable) {
        super(callable);
    }

    public static <T> ArrayFuture<T> create(Producer<T> producer, Worker<T> worker, final T[] array) {
        return new ArrayFuture<>(new RunCallable<T>(producer.producerExecutor(), worker, producer.createObserver(), new Array<T>(array)));
    }

    public static <T> ArrayFuture<T> create(Producer<T> producer, Worker<T> worker, final java.util.List<T> list) {
        return new ArrayFuture<>(new RunCallable<T>(producer.producerExecutor(), worker, producer.createObserver(), new List<T>(list)));
    }

    private static final class Array<T> implements ArrayData<T> {
        T[] array;

        Array(T[] array) {
            this.array = array;
        }

        @Override
        public T getData(int index) {
            return array[index];
        }

        @Override
        public int length() {
            return array.length;
        }
    }

    private static final class List<T> implements ArrayData<T> {
        java.util.List<T> array;

        List(java.util.List<T> array) {
            this.array = array;
        }

        @Override
        public T getData(int index) {
            return array.get(index);
        }

        @Override
        public int length() {
            return array.size();
        }
    }

    private static final class RunCallable<T> extends WorkerFuture.RunCallable<T> {
        final ArrayData<T> arrayData;

        RunCallable(ThreadPoolExecutor executor, Worker<T> worker, ProducerObserver observer, ArrayData<T> arrayData) {
            super(executor, worker, observer);
            this.arrayData = arrayData;
        }

        @Override
        protected AtomicLatch createLatch() {
            return new Latch(new CountDownLatch(arrayData.length()));
        }

        @Override
        protected AtomicCounter<T> createCounter(AtomicLatch latch) {
            return new ArrayCounter<>(arrayData, latch);
        }
    }

    private static class ArrayCounter<T> implements AtomicCounter<T> {
        final ArrayData<T> arrayData;
        final AtomicInteger atomic;
        final AtomicLatch latch;

        public ArrayCounter(ArrayData<T> array, AtomicLatch latch) {
            this.arrayData = array;
            this.atomic = new AtomicInteger(0);
            this.latch = latch;
        }

        @Override
        public boolean checkCounter(T data) {
            return data != null;
        }

        @Override
        public void countDown() {
            latch.countDown();
        }

        @Override
        public T getAndCounter() {
            int i = atomic.getAndIncrement();
            if (i < arrayData.length()) {
                return arrayData.getData(i);
            }
            return null;
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
