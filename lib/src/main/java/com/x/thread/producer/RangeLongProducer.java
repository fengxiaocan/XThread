package com.x.thread.producer;

import com.x.thread.function.Future;
import com.x.thread.function.Worker;

import java.util.concurrent.FutureTask;

public final class RangeLongProducer extends Producer<Long> {
    private final long start;
    private final long end;

    public RangeLongProducer(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Future execute(Worker<Long> worker) {
        FutureTask<Long> future = LongFuture.create(this, worker, start, end);
        coreExecutor().execute(future);
        return new ExecutorFuture(future);
    }
}
