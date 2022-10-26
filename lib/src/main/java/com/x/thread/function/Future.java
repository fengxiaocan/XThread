package com.x.thread.function;

import java.util.concurrent.TimeUnit;

public interface Future {
    boolean cancel();

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    void await(Callback callback);

    void await(long var1, Callback callback);

    void await(long var1, TimeUnit var3, Callback callback);

    interface Callback {
        void onFinish(long time);
    }
}
