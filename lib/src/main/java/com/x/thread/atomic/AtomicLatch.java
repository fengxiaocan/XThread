package com.x.thread.atomic;

import java.util.concurrent.TimeUnit;

public interface AtomicLatch {
    void await() throws InterruptedException;

    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    void countDown();
}
