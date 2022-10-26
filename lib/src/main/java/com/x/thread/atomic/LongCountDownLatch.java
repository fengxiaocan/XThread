package com.x.thread.atomic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

public class LongCountDownLatch {
    private final LongCountDownLatch.Sync sync;

    public LongCountDownLatch(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        } else {
            this.sync = new LongCountDownLatch.Sync(count);
        }
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public long getCount() {
        return this.sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + this.sync.getCount() + "]";
    }

    private static final class Sync extends AbstractQueuedLongSynchronizer {
        private static final long serialVersionUID = 4982264981988014374L;

        Sync(long count) {
            this.setState(count);
        }

        long getCount() {
            return this.getState();
        }

        @Override
        protected long tryAcquireShared(long arg) {
            return this.getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(long releases) {
            long c;
            long nextc;
            do {
                c = this.getState();
                if (c == 0) {
                    return false;
                }

                nextc = c - 1;
            } while (!this.compareAndSetState(c, nextc));

            return nextc == 0;
        }
    }
}
