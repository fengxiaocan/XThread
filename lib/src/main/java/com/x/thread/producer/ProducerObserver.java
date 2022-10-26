package com.x.thread.producer;

import com.x.thread.function.Observer;
import com.x.thread.scheduler.Scheduler;

final class ProducerObserver implements Observer {
    final Observer observer;
    final Scheduler scheduler;

    public ProducerObserver(Observer observer, Scheduler scheduler) {
        this.observer = observer;
        this.scheduler = scheduler;
    }

    @Override
    public void onCancel() {
        if (scheduler != null && observer != null) {
            scheduler.onScheduler(new Runnable() {
                @Override
                public void run() {
                    observer.onCancel();
                }
            });
        } else {
            if (observer != null) observer.onCancel();
        }
    }

    @Override
    public void onError(final Throwable e) {
        if (scheduler != null && observer != null) {
            scheduler.onScheduler(new Runnable() {
                @Override
                public void run() {
                    observer.onError(e);
                }
            });
        } else {
            if (observer != null) observer.onError(e);
        }
    }

    @Override
    public void onComplete() {
        if (scheduler != null && observer != null) {
            scheduler.onScheduler(new Runnable() {
                @Override
                public void run() {
                    observer.onComplete();
                }
            });
        } else {
            if (observer != null) observer.onComplete();
        }
    }

}
