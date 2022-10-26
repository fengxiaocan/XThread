package com.x.thread.producer;

import com.x.thread.atomic.AtomicCounter;
import com.x.thread.function.Cancelled;
import com.x.thread.function.Worker;

final class ProducerRunnable<T> implements Runnable, Cancelled {
    private final Worker<T> worker;
    private final ProducerObserver observer;
    private final AtomicCounter<T> counter;
    private volatile boolean isCancel;

    public ProducerRunnable(Worker<T> worker, ProducerObserver observer, AtomicCounter<T> counter) {
        this.worker = worker;
        this.observer = observer;
        this.counter = counter;
        this.isCancel = false;
    }

    @Override
    public void run() {
        T index = counter.getAndCounter();
        while (!isCancel && counter.checkCounter(index)) {
            try {
                worker.onExecutor(index);
            } catch (Throwable e) {
                observer.onError(e);
            }
            counter.countDown();
            index = counter.getAndCounter();
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
        if (worker instanceof Cancelled) {
            ((Cancelled) worker).cancel();
        }
    }

}
