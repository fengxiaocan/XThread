package com.x.thread.producer;

import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;
import com.x.thread.thread.BinaryThreadPoolExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public final class RangeProducer extends Producer<Integer> {
    private final int start;
    private final int end;

    public RangeProducer(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public RxFuture<Long> execute(Worker<Integer> worker) {
        FutureTask<Long> future = IntegerFuture.create(this, worker, start, end);
        ExecutorService executor = coreExecutor();
        if (executor instanceof BinaryThreadPoolExecutor) {
            ((BinaryThreadPoolExecutor) executor).execute(future, isCore());
        } else {
            executor.execute(future);
        }
        return new TimeFuture(future);
    }
}
