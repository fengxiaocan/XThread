package com.x.thread.execute;

import com.x.thread.function.RxFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ExecutorSource {
    <T> RxFuture<T> submit(Callable<T> var1);

    <T> RxFuture<T> submit(Runnable var1, T var2);

    RxFuture<?> submit(Runnable var1);

    void execute(Runnable var1);

    <T> RxFuture<T> submit(Callable<T> var1,boolean isCore);

    <T> RxFuture<T> submit(Runnable var1, T var2, boolean isCore);

    RxFuture<?> submit(Runnable var1, boolean isCore);

    void execute(Runnable var1, boolean isCore);
}
