package com.x.thread.producer;

import com.x.thread.function.RxFuture;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class TimeFuture implements RxFuture<Long> {
    private final Future<Long> future;

    public TimeFuture(Future<Long> future) {
        this.future = future;
    }

    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public void await(final Callback<Long> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = -1;
                try {
                    time = future.get();
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(time);
                    }
                }
            }
        }).start();
    }

    @Override
    public void await(final long var1, final Callback<Long> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = -1;
                try {
                    time = future.get(var1, TimeUnit.MILLISECONDS);
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(time);
                    }
                }
            }
        }).start();
    }

    @Override
    public void await(final long var1, final TimeUnit var3, final Callback<Long> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = -1;
                try {
                    time = future.get(var1, var3);
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(time);
                    }
                }
            }
        }).start();
    }
}
