package com.x.thread.provide;

import java.util.concurrent.atomic.AtomicInteger;

final class ObserverRunnable implements Runnable {
    private final Runnable runnable;
    private final AtomicInteger atomic;
    private final Observer observer;

    public ObserverRunnable(Runnable runnable, AtomicInteger atomic, Observer observer) {
        this.runnable = runnable;
        this.atomic = atomic;
        this.observer = observer;
        atomic.getAndIncrement();
    }

    @Override
    public void run() {
        this.runnable.run();
        final int index = atomic.decrementAndGet();
        if (index <= 0) {
            observer.onComplete();
        }
    }
}
