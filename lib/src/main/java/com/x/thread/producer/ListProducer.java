package com.x.thread.producer;

import com.x.thread.function.Future;
import com.x.thread.function.Worker;

import java.util.List;
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
    public Future execute(Worker<T> worker) {
        FutureTask<Long> future = ArrayFuture.create(this, worker, list);
        coreExecutor().execute(future);
        return new ExecutorFuture(future);
    }
}
