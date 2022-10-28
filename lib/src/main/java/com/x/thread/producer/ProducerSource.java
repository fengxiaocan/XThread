package com.x.thread.producer;

import com.x.thread.function.RxFuture;
import com.x.thread.function.Worker;

public interface ProducerSource<T> {
    RxFuture<Long> execute(Worker<T> worker);
}
