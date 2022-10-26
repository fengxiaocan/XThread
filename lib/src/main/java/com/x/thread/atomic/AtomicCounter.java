package com.x.thread.atomic;

public interface AtomicCounter<T> {
    boolean checkCounter(T t);

    void countDown();

    T getAndCounter();
}
