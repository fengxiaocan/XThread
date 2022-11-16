package com.x.thread.provide;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

final class ObserverCallback<V> implements Callable<V> {
    private final Callable<V> runnable;
    private final AtomicInteger atomic;
    private final Observer observer;

    public ObserverCallback(Callable<V> runnable, AtomicInteger atomic, Observer observer) {
        this.runnable = runnable;
        this.atomic = atomic;
        this.observer = observer;
        atomic.getAndIncrement();
    }

    @Override
    public V call() throws Exception {
        V call = this.runnable.call();
        final int index = atomic.decrementAndGet();
        if (index <= 0) {
            observer.onComplete();
        }
        return call;
    }
}
