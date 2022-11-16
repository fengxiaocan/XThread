package com.x.thread.producer;

import com.x.thread.execute.DequeThreadPoolExecutor;
import com.x.thread.execute.PriorityThreadPoolExecutor;
import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;

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
        executor.execute(future);
        return new TimeFuture(future);
    }

    @Override
    public RxFuture<Long> priorityExecute(Worker<Integer> worker) {
        FutureTask<Long> future = IntegerFuture.create(this, worker, start, end);
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
