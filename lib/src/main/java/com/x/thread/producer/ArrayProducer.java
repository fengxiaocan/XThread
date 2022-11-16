package com.x.thread.producer;

import com.x.thread.execute.DequeThreadPoolExecutor;
import com.x.thread.execute.PriorityThreadPoolExecutor;
import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;

import java.util.concurrent.ExecutorService;
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
    public RxFuture<Long> execute(Worker<T> worker) {
        FutureTask<Long> future = ArrayFuture.create(this, worker, array);
        ExecutorService executor = coreExecutor();
        executor.execute(future);
        return new TimeFuture(future);
    }

    @Override
    public RxFuture<Long> priorityExecute(Worker<T> worker) {
        FutureTask<Long> future = ArrayFuture.create(this, worker, array);
        ExecutorService executor = coreExecutor();
        if (executor instanceof PriorityThreadPoolExecutor) {
            ((PriorityThreadPoolExecutor) executor).execute(future, true);
        } else if (executor instanceof DequeThreadPoolExecutor) {
            ((DequeThreadPoolExecutor) executor).execute(future, true);
        } else {
            executor.execute(future);
        }
        return new TimeFuture(future);
    }
}
