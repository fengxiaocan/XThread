package com.x.thread;

import com.x.thread.execute.ExecutorProvide;
import com.x.thread.producer.ArrayProducer;
import com.x.thread.producer.ListProducer;
import com.x.thread.producer.Producer;
import com.x.thread.producer.RangeLongProducer;
import com.x.thread.producer.RangeProducer;

import java.util.List;

public final class RxThread {

    public static <T> Producer<T> from(T... array) {
        return new ArrayProducer<>(array);
    }

    public static <T> Producer<T> from(List<T> list) {
        return new ListProducer<>(list);
    }

    public static Producer<Integer> create(int taskCount) {
        return new RangeProducer(1, taskCount);
    }

    public static Producer<Integer> range(final int start, int end) {
        return new RangeProducer(start, end);
    }

    public static Producer<Long> range(final long start, long end) {
        return new RangeLongProducer(start, end);
    }

    public static ExecutorProvide executor(int coreCount) {
        return new ExecutorProvide(coreCount);
    }

    public static ExecutorProvide executor() {
        return new ExecutorProvide();
    }

}
