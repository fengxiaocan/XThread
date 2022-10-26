package com.x.thread.producer;

import com.x.thread.function.Future;
import com.x.thread.function.Worker;

import java.util.concurrent.FutureTask;

public final class ArrayProducer<T> extends Producer<T> {
    private final T[] array;

    public ArrayProducer(T[] array) {
        if (array == null || array.length == 0) {
            throw new NullPointerException("array not is empty");
        }
        this.array = array;

    }

    @Override
    public Future execute(Worker<T> worker) {
        FutureTask<Long> future = ArrayFuture.create(this, worker, array);
        coreExecutor().execute(future);
        return new ExecutorFuture(future);
    }
}
