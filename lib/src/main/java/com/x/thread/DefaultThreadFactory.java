package com.x.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class DefaultThreadFactory implements ThreadFactory {
    private static final String THREAD_GROUP = "XThread";
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int priority;
    private final boolean isDaemon;

    DefaultThreadFactory() {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("XThread-No.%d-", poolNumber.getAndIncrement());
        priority = 5;
        isDaemon = false;
    }

    DefaultThreadFactory(String name) {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("XThread-%s-No.%d-", name, poolNumber.getAndIncrement());
        priority = 5;
        isDaemon = false;
    }


    DefaultThreadFactory(String name, int priority, boolean isDaemon) {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("XThread-%s-No.%d-", name, poolNumber.getAndIncrement());
        this.priority = priority;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        t.setDaemon(isDaemon);
        t.setPriority(priority);
        return t;
    }
}