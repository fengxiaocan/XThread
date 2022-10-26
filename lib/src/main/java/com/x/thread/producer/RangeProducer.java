package com.x.thread.producer;

import com.x.thread.function.Future;
import com.x.thread.function.Worker;

import java.util.concurrent.FutureTask;

public final class RangeProducer extends Producer<Integer> {
    private final int start;
    private final int end;

    public RangeProducer(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Future execute(Worker<Integer> worker) {
        FutureTask<Long> future = IntegerFuture.create(this, worker, start, end);
        coreExecutor().execute(future);
        return new ExecutorFuture(future);
    }
}
