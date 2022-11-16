package com.x.thread.provide;

import com.x.thread.function.RxFuture;

import java.util.concurrent.Callable;

public interface ExecutorSource {
    <T> RxFuture<T> submit(Callable<T> var1);

    <T> RxFuture<T> submit(Runnable var1, T var2);

    RxFuture<?> submit(Runnable var1);

    void execute(Runnable var1);

    <T> RxFuture<T> prioritySubmit(Callable<T> var1);

    <T> RxFuture<T> prioritySubmit(Runnable var1, T var2);

    RxFuture<?> prioritySubmit(Runnable var1);

    void priorityExecute(Runnable var1);
}
