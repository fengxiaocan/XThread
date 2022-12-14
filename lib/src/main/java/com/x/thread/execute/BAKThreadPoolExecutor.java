package com.x.thread.execute;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BAKThreadPoolExecutor extends AbstractExecutorService {
    protected static final int COUNT_BITS = 29;
    protected static final int COUNT_MASK = 536870911;
    protected static final int RUNNING = -536870912;
    protected static final int SHUTDOWN = 0;
    protected static final int STOP = 536870912;
    protected static final int TIDYING = 1073741824;
    protected static final int TERMINATED = 1610612736;
    protected static final RejectedExecutionHandler defaultHandler = new BAKThreadPoolExecutor.AbortPolicy();
    protected static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
    protected static final boolean ONLY_ONE = true;
    protected final AtomicInteger ctl;
    protected final BlockingQueue<Runnable> workQueue;
    protected final ReentrantLock mainLock;
    protected final HashSet<BAKThreadPoolExecutor.Worker> workers;
    protected final Condition termination;
    protected int largestPoolSize;
    protected long completedTaskCount;
    protected volatile ThreadFactory threadFactory;
    protected volatile RejectedExecutionHandler handler;
    protected volatile long keepAliveTime;
    protected volatile boolean allowCoreThreadTimeOut;
    protected volatile int corePoolSize;
    protected volatile int maximumPoolSize;

    public BAKThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    public BAKThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    public BAKThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public BAKThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        this.ctl = new AtomicInteger(ctlOf(RUNNING, SHUTDOWN));
        this.mainLock = new ReentrantLock();
        this.workers = new HashSet();
        this.termination = this.mainLock.newCondition();
        if (corePoolSize >= 0 && maximumPoolSize > 0 && maximumPoolSize >= corePoolSize && keepAliveTime >= 0L) {
            if (workQueue != null && threadFactory != null && handler != null) {
                this.corePoolSize = corePoolSize;
                this.maximumPoolSize = maximumPoolSize;
                this.workQueue = workQueue;
                this.keepAliveTime = unit.toNanos(keepAliveTime);
                this.threadFactory = threadFactory;
                this.handler = handler;
            } else {
                throw new NullPointerException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected static int runStateOf(int c) {
        return c & RUNNING;
    }

    protected static int workerCountOf(int c) {
        return c & COUNT_MASK;
    }

    protected static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    protected static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    protected static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    protected static boolean isRunning(int c) {
        return c < 0;
    }

    protected boolean compareAndIncrementWorkerCount(int expect) {
        return this.ctl.compareAndSet(expect, expect + 1);
    }

    protected boolean compareAndDecrementWorkerCount(int expect) {
        return this.ctl.compareAndSet(expect, expect - 1);
    }

    protected void decrementWorkerCount() {
        this.ctl.addAndGet(-1);
    }

    protected void advanceRunState(int targetState) {
        int c;
        do {
            c = this.ctl.get();
        } while (!runStateAtLeast(c, targetState) && !this.ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))));

    }

    protected final void tryTerminate() {
        while (true) {
            int c = this.ctl.get();
            if (isRunning(c) || runStateAtLeast(c, TIDYING) || runStateLessThan(c, STOP) && !this.workQueue.isEmpty()) {
                return;
            }

            if (workerCountOf(c) != 0) {
                this.interruptIdleWorkers(ONLY_ONE);
                return;
            }

            ReentrantLock mainLock = this.mainLock;
            mainLock.lock();

            try {
                if (!this.ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    continue;
                }

                try {
                    this.terminated();
                } finally {
                    this.ctl.set(ctlOf(TERMINATED, 0));
                    this.termination.signalAll();
                }
            } finally {
                mainLock.unlock();
            }

            return;
        }
    }

    protected void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            Iterator var2 = this.workers.iterator();

            while (var2.hasNext()) {
                BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var2.next();
                security.checkAccess(w.thread);
            }
        }

    }

    protected void interruptWorkers() {
        Iterator var1 = this.workers.iterator();

        while (var1.hasNext()) {
            BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var1.next();
            w.interruptIfStarted();
        }

    }

    protected void interruptIdleWorkers(boolean onlyOne) {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            Iterator var3 = this.workers.iterator();

            while (var3.hasNext()) {
                BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var3.next();
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException var15) {
                    } finally {
                        w.unlock();
                    }
                }

                if (onlyOne) {
                    break;
                }
            }
        } finally {
            mainLock.unlock();
        }

    }

    protected void interruptIdleWorkers() {
        this.interruptIdleWorkers(false);
    }

    protected final void reject(Runnable command) {
        this.handler.rejectedExecution(command, this);
    }

    protected void onShutdown() {
    }

    protected List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = this.workQueue;
        ArrayList<Runnable> taskList = new ArrayList();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            Runnable[] var3 = (Runnable[]) q.toArray(new Runnable[0]);
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Runnable r = var3[var5];
                if (q.remove(r)) {
                    taskList.add(r);
                }
            }
        }

        return taskList;
    }

    protected boolean addWorker(Runnable firstTask, boolean core) {
        int c = this.ctl.get();

        label247:
        while (!runStateAtLeast(c, 0) || !runStateAtLeast(c, STOP) && firstTask == null && !this.workQueue.isEmpty()) {
            while (workerCountOf(c) < ((core ? this.corePoolSize : this.maximumPoolSize) & COUNT_MASK)) {
                if (this.compareAndIncrementWorkerCount(c)) {
                    boolean workerStarted = false;
                    boolean workerAdded = false;
                    BAKThreadPoolExecutor.Worker w = null;

                    try {
                        w = new BAKThreadPoolExecutor.Worker(firstTask);
                        Thread t = w.thread;
                        if (t != null) {
                            ReentrantLock mainLock = this.mainLock;
                            mainLock.lock();

                            try {
                                c = this.ctl.get();
                                if (isRunning(c) || runStateLessThan(c, STOP) && firstTask == null) {
                                    if (t.getState() != Thread.State.NEW) {
                                        throw new IllegalThreadStateException();
                                    }

                                    this.workers.add(w);
                                    workerAdded = true;
                                    int s = this.workers.size();
                                    if (s > this.largestPoolSize) {
                                        this.largestPoolSize = s;
                                    }
                                }
                            } finally {
                                mainLock.unlock();
                            }

                            if (workerAdded) {
                                t.start();
                                workerStarted = true;
                            }
                        }
                    } finally {
                        if (!workerStarted) {
                            this.addWorkerFailed(w);
                        }

                    }

                    return workerStarted;
                }

                c = this.ctl.get();
                if (runStateAtLeast(c, 0)) {
                    continue label247;
                }
            }

            return false;
        }

        return false;
    }

    protected void addWorkerFailed(BAKThreadPoolExecutor.Worker w) {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            if (w != null) {
                this.workers.remove(w);
            }

            this.decrementWorkerCount();
            this.tryTerminate();
        } finally {
            mainLock.unlock();
        }

    }

    protected void processWorkerExit(BAKThreadPoolExecutor.Worker w, boolean completedAbruptly) {
        if (completedAbruptly) {
            this.decrementWorkerCount();
        }

        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            this.completedTaskCount += w.completedTasks;
            this.workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        this.tryTerminate();
        int c = this.ctl.get();
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = this.allowCoreThreadTimeOut ? 0 : this.corePoolSize;
                if (min == 0 && !this.workQueue.isEmpty()) {
                    min = 1;
                }

                if (workerCountOf(c) >= min) {
                    return;
                }
            }

            this.addWorker((Runnable) null, false);
        }

    }

    protected Runnable getTask() {
        boolean timedOut = false;

        while (true) {
            int c = this.ctl.get();
            if (runStateAtLeast(c, 0) && (runStateAtLeast(c, STOP) || this.workQueue.isEmpty())) {
                this.decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);
            boolean timed = this.allowCoreThreadTimeOut || wc > this.corePoolSize;
            if (wc <= this.maximumPoolSize && (!timed || !timedOut) || wc <= 1 && !this.workQueue.isEmpty()) {
                try {
                    Runnable r = timed ? (Runnable) this.workQueue.poll(this.keepAliveTime, TimeUnit.NANOSECONDS) : (Runnable) this.workQueue.take();
                    if (r != null) {
                        return r;
                    }

                    timedOut = true;
                } catch (InterruptedException var6) {
                    timedOut = false;
                }
            } else if (this.compareAndDecrementWorkerCount(c)) {
                return null;
            }
        }
    }

    protected final void runWorker(BAKThreadPoolExecutor.Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock();
        boolean completedAbruptly = true;

        try {
            while (task != null || (task = this.getTask()) != null) {
                w.lock();
                if ((runStateAtLeast(this.ctl.get(), STOP) || Thread.interrupted() && runStateAtLeast(this.ctl.get(), STOP)) && !wt.isInterrupted()) {
                    wt.interrupt();
                }

                try {
                    this.beforeExecute(wt, task);

                    try {
                        task.run();
                        this.afterExecute(task, (Throwable) null);
                    } catch (Throwable var14) {
                        this.afterExecute(task, var14);
                        throw var14;
                    }
                } finally {
                    task = null;
                    ++w.completedTasks;
                    w.unlock();
                }
            }

            completedAbruptly = false;
        } finally {
            this.processWorkerExit(w, completedAbruptly);
        }

    }

    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        } else {
            int c = this.ctl.get();
            if (workerCountOf(c) < this.corePoolSize) {
                if (this.addWorker(command, true)) {
                    return;
                }

                c = this.ctl.get();
            }

            if (isRunning(c) && this.workQueue.offer(command)) {
                int recheck = this.ctl.get();
                if (!isRunning(recheck) && this.remove(command)) {
                    this.reject(command);
                } else if (workerCountOf(recheck) == 0) {
                    this.addWorker((Runnable) null, false);
                }
            } else if (!this.addWorker(command, false)) {
                this.reject(command);
            }

        }
    }

    public void shutdown() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            this.checkShutdownAccess();
            this.advanceRunState(0);
            this.interruptIdleWorkers();
            this.onShutdown();
        } finally {
            mainLock.unlock();
        }

        this.tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        List tasks;
        try {
            this.checkShutdownAccess();
            this.advanceRunState(STOP);
            this.interruptWorkers();
            tasks = this.drainQueue();
        } finally {
            mainLock.unlock();
        }

        this.tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return runStateAtLeast(this.ctl.get(), 0);
    }

    protected boolean isStopped() {
        return runStateAtLeast(this.ctl.get(), STOP);
    }

    public boolean isTerminating() {
        int c = this.ctl.get();
        return runStateAtLeast(c, 0) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(this.ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        boolean var7;
        try {
            while (runStateLessThan(this.ctl.get(), TERMINATED)) {
                if (nanos <= 0L) {
                    var7 = false;
                    return var7;
                }

                nanos = this.termination.awaitNanos(nanos);
            }

            var7 = true;
        } finally {
            mainLock.unlock();
        }

        return var7;
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException();
        } else {
            this.threadFactory = threadFactory;
        }
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.handler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        } else {
            this.handler = handler;
        }
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize >= 0 && this.maximumPoolSize >= corePoolSize) {
            int delta = corePoolSize - this.corePoolSize;
            this.corePoolSize = corePoolSize;
            if (workerCountOf(this.ctl.get()) > corePoolSize) {
                this.interruptIdleWorkers();
            } else if (delta > 0) {
                int var3 = Math.min(delta, this.workQueue.size());

                while (var3-- > 0 && this.addWorker((Runnable) null, true) && !this.workQueue.isEmpty()) {
                }
            }

        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean prestartCoreThread() {
        return workerCountOf(this.ctl.get()) < this.corePoolSize && this.addWorker((Runnable) null, true);
    }

    protected void ensurePrestart() {
        int wc = workerCountOf(this.ctl.get());
        if (wc < this.corePoolSize) {
            this.addWorker((Runnable) null, true);
        } else if (wc == 0) {
            this.addWorker((Runnable) null, false);
        }

    }

    public int prestartAllCoreThreads() {
        int n;
        for (n = 0; this.addWorker((Runnable) null, true); ++n) {
        }

        return n;
    }

    public boolean allowsCoreThreadTimeOut() {
        return this.allowCoreThreadTimeOut;
    }

    public void allowCoreThreadTimeOut(boolean value) {
        if (value && this.keepAliveTime <= 0L) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        } else {
            if (value != this.allowCoreThreadTimeOut) {
                this.allowCoreThreadTimeOut = value;
                if (value) {
                    this.interruptIdleWorkers();
                }
            }

        }
    }

    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize > 0 && maximumPoolSize >= this.corePoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            if (workerCountOf(this.ctl.get()) > maximumPoolSize) {
                this.interruptIdleWorkers();
            }

        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0L) {
            throw new IllegalArgumentException();
        } else if (time == 0L && this.allowsCoreThreadTimeOut()) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        } else {
            long keepAliveTime = unit.toNanos(time);
            long delta = keepAliveTime - this.keepAliveTime;
            this.keepAliveTime = keepAliveTime;
            if (delta < 0L) {
                this.interruptIdleWorkers();
            }

        }
    }

    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(this.keepAliveTime, TimeUnit.NANOSECONDS);
    }

    public BlockingQueue<Runnable> getQueue() {
        return this.workQueue;
    }

    public boolean remove(Runnable task) {
        boolean removed = this.workQueue.remove(task);
        this.tryTerminate();
        return removed;
    }

    public void purge() {
        BlockingQueue q = this.workQueue;

        try {
            Iterator it = q.iterator();

            while (it.hasNext()) {
                Runnable r = (Runnable) it.next();
                if (r instanceof Future && ((Future) r).isCancelled()) {
                    it.remove();
                }
            }
        } catch (ConcurrentModificationException var7) {
            Object[] var3 = q.toArray();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Object r = var3[var5];
                if (r instanceof Future && ((Future) r).isCancelled()) {
                    q.remove(r);
                }
            }
        }

        this.tryTerminate();
    }

    public int getPoolSize() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        int var2;
        try {
            var2 = runStateAtLeast(this.ctl.get(), 1073741824) ? 0 : this.workers.size();
        } finally {
            mainLock.unlock();
        }

        return var2;
    }

    public int getActiveCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            int n = 0;
            Iterator var3 = this.workers.iterator();

            while (var3.hasNext()) {
                BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var3.next();
                if (w.isLocked()) {
                    ++n;
                }
            }

            int var8 = n;
            return var8;
        } finally {
            mainLock.unlock();
        }
    }

    public int getLargestPoolSize() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        int var2;
        try {
            var2 = this.largestPoolSize;
        } finally {
            mainLock.unlock();
        }

        return var2;
    }

    public long getTaskCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            long n = this.completedTaskCount;
            Iterator var4 = this.workers.iterator();

            while (var4.hasNext()) {
                BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var4.next();
                n += w.completedTasks;
                if (w.isLocked()) {
                    ++n;
                }
            }

            long var9 = n + (long) this.workQueue.size();
            return var9;
        } finally {
            mainLock.unlock();
        }
    }

    public long getCompletedTaskCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        try {
            long n = this.completedTaskCount;

            BAKThreadPoolExecutor.Worker w;
            for (Iterator var4 = this.workers.iterator(); var4.hasNext(); n += w.completedTasks) {
                w = (BAKThreadPoolExecutor.Worker) var4.next();
            }

            long var9 = n;
            return var9;
        } finally {
            mainLock.unlock();
        }
    }

    public String toString() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();

        long ncompleted;
        int nworkers;
        int nactive;
        try {
            ncompleted = this.completedTaskCount;
            nactive = 0;
            nworkers = this.workers.size();
            Iterator var6 = this.workers.iterator();

            while (var6.hasNext()) {
                BAKThreadPoolExecutor.Worker w = (BAKThreadPoolExecutor.Worker) var6.next();
                ncompleted += w.completedTasks;
                if (w.isLocked()) {
                    ++nactive;
                }
            }
        } finally {
            mainLock.unlock();
        }

        int c = this.ctl.get();
        String runState = isRunning(c) ? "Running" : (runStateAtLeast(c, TERMINATED) ? "Terminated" : "Shutting down");
        return super.toString() + "[" + runState + ", pool size = " + nworkers + ", active threads = " + nactive + ", queued tasks = " + this.workQueue.size() + ", completed tasks = " + ncompleted + "]";
    }

    protected void beforeExecute(Thread t, Runnable r) {
    }

    protected void afterExecute(Runnable r, Throwable t) {
    }

    protected void terminated() {
    }

    public interface RejectedExecutionHandler {
        void rejectedExecution(Runnable r, BAKThreadPoolExecutor e);
    }

    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        public DiscardOldestPolicy() {
        }

        public void rejectedExecution(Runnable r, BAKThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }

        }
    }

    public static class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() {
        }

        public void rejectedExecution(Runnable r, BAKThreadPoolExecutor e) {
        }
    }

    public static class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() {
        }

        public void rejectedExecution(Runnable r, BAKThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
        }
    }

    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() {
        }

        public void rejectedExecution(Runnable r, BAKThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }

        }
    }

    protected final class Worker extends AbstractQueuedSynchronizer implements Runnable {
        private static final long serialVersionUID = 6138294804551838833L;
        final Thread thread;
        Runnable firstTask;
        volatile long completedTasks;

        Worker(Runnable firstTask) {
            this.setState(-1);
            this.firstTask = firstTask;
            this.thread = BAKThreadPoolExecutor.this.getThreadFactory().newThread(this);
        }

        public void run() {
            BAKThreadPoolExecutor.this.runWorker(this);
        }

        protected boolean isHeldExclusively() {
            return this.getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (this.compareAndSetState(0, 1)) {
                this.setExclusiveOwnerThread(Thread.currentThread());
                return true;
            } else {
                return false;
            }
        }

        protected boolean tryRelease(int unused) {
            this.setExclusiveOwnerThread((Thread) null);
            this.setState(0);
            return true;
        }

        public void lock() {
            this.acquire(1);
        }

        public boolean tryLock() {
            return this.tryAcquire(1);
        }

        public void unlock() {
            this.release(1);
        }

        public boolean isLocked() {
            return this.isHeldExclusively();
        }

        void interruptIfStarted() {
            Thread t;
            if (this.getState() >= 0 && (t = this.thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException var3) {
                }
            }

        }
    }
}
