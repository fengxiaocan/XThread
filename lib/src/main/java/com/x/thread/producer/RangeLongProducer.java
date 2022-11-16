package com.x.thread.producer;

import com.x.thread.execute.DequeThreadPoolExecutor;
import com.x.thread.execute.PriorityThreadPoolExecutor;
import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public final class RangeLongProducer extends Producer<Long> {
    private final long start;
    private final long end;

    public RangeLongProducer(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public RxFuture<Long> execute(Worker<Long> worker) {
        FutureTask<Long> future = LongFuture.create(this, worker, start, end);
        ExecutorService executor = coreExecutor();
        executor.execute(future);
        return new TimeFuture(future);
    }

    @Override
    public RxFuture<Long> priorityExecute(Worker<Long> worker) {
        FutureTask<Long> future = LongFuture.create(this, worker, start, end);
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
