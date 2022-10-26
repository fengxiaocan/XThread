package com.x.thread.scheduler;

public final class Schedulers {
    public static Scheduler newThread() {
        return new NewThreadSchedulers();
    }

    static final class NewThreadSchedulers implements Scheduler {
        @Override
        public void onScheduler(Runnable runnable) {
            new Thread(runnable).start();
        }
    }
}
