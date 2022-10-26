package com.x.thread.producer;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class ExecutorFuture implements com.x.thread.function.Future {
    private final Future<Long> future;

    public ExecutorFuture(Future<Long> future) {
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
    public void await(final Callback callback) {
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
    public void await(final long var1, final Callback callback) {
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
    public void await(final long var1, final TimeUnit var3, final Callback callback) {
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
