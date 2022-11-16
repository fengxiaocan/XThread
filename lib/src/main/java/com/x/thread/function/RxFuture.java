package com.x.thread.function;

import java.util.concurrent.TimeUnit;

public interface RxFuture<R> {

    boolean cancel();

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    void await(Callback<R> callback);

    void await(long var1, Callback<R> callback);

    void await(long var1, TimeUnit var3, Callback<R> callback);

    interface Callback<R> {
        void onFinish(R r);
    }

}
