package com.x.thread.producer;

import com.x.thread.function.Future;
import com.x.thread.function.Worker;

public interface ProducerSource<T> {
    Future execute(Worker<T> worker);
}
