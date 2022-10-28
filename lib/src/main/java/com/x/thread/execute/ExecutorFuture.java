package com.x.thread.execute;

import com.x.thread.function.RxFuture;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class ExecutorFuture<R> implements RxFuture<R> {
    private final Future<R> future;

    public ExecutorFuture(Future<R> future) {
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
    public void await(final Callback<R> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                R value = null;
                try {
                    value = future.get();
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(value);
                    }
                }
            }
        }).start();
    }

    @Override
    public void await(final long var1, final Callback<R> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                R value = null;
                try {
                    value = future.get(var1, TimeUnit.MILLISECONDS);
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(value);
                    }
                }
            }
        }).start();
    }

    @Override
    public void await(final long var1, final TimeUnit var3, final Callback<R> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                R value = null;
                try {
                    value = future.get(var1, var3);
                } catch (Throwable e) {
                    cancel();
                } finally {
                    if (callback != null) {
                        callback.onFinish(value);
                    }
                }
            }
        }).start();
    }
}
