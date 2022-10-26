package com.x.thread.function;

public interface Observer {
    void onCancel();

    void onError(Throwable e);

    void onComplete();
}
