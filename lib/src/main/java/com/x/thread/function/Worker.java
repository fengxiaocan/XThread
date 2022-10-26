package com.x.thread.function;

public interface Worker<T> {
    void onExecutor(T data) throws Throwable;
}
