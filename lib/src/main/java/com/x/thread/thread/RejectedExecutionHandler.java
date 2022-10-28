package com.x.thread.thread;

public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable var1, BinaryThreadPoolExecutor var2);
}
