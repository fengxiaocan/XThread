package com.x.thread.producer;

import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;
import com.x.thread.thread.BinaryThreadPoolExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public final class ListProducer<T> extends Producer<T> {
    private final List<T> list;

    public ListProducer(List<T> list) {
        if (list == null || list.size() == 0) {
            throw new NullPointerException("array not is empty");
        }
        this.list = list;
    }

    @Override
    public RxFuture<Long> execute(Worker<T> worker) {
        FutureTask<Long> future = ArrayFuture.create(this, worker, list);
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor) {
            ((BinaryThreadPoolExecutor) executor).execute(future, isCore());
        } else {
            executor.execute(future);
        }
        return new TimeFuture(future);
    }
}
