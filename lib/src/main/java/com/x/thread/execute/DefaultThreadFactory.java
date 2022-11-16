package com.x.thread.execute;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    protected static final String THREAD_GROUP = "XThread";
    protected static final AtomicInteger poolNumber = new AtomicInteger(1);
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final String namePrefix;
    protected final int priority;
    protected final boolean isDaemon;

    public DefaultThreadFactory() {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("XThread-G%d-T", poolNumber.getAndIncrement());
        priority = 5;
        isDaemon = false;
    }

    public DefaultThreadFactory(String name) {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("%s-G%d-T", name, poolNumber.getAndIncrement());
        priority = 5;
        isDaemon = false;
    }


    public DefaultThreadFactory(String name, int priority, boolean isDaemon) {
        this.group = new ThreadGroup(THREAD_GROUP);
        this.namePrefix = String.format("%s-G%d-T", name, poolNumber.getAndIncrement());
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