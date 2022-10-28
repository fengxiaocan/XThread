package com.x.thread.thread;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BinaryQueue<E> implements ThreadQueue<E> {
    private final int capacity;
    private final AtomicInteger count;
    private final ReentrantLock takeLock;
    private final Condition notEmpty;
    private final ReentrantLock putLock;
    private final Condition notFull;
    private final QueueNode<E> queue = new QueueNode<>();
    private final QueueNode<E> core = new QueueNode<>();

    public BinaryQueue() {
        this(2147483647);
    }

    public BinaryQueue(int capacity) {
        this.count = new AtomicInteger();
        this.takeLock = new ReentrantLock();
        this.notEmpty = this.takeLock.newCondition();
        this.putLock = new ReentrantLock();
        this.notFull = this.putLock.newCondition();
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.capacity = capacity;
            queue.init();
            core.init();
        }
    }

    public BinaryQueue(Collection<? extends E> c) {
        this(2147483647);
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            int n = 0;
            for (Iterator var4 = c.iterator(); var4.hasNext(); ++n) {
                Object e = var4.next();
                if (e == null) {
                    throw new NullPointerException();
                }

                if (n == this.capacity) {
                    throw new IllegalStateException("Queue full");
                }
                queue.enqueue((E) e);
            }

            this.count.set(n);
        } finally {
            putLock.unlock();
        }
    }

    private void signalNotEmpty() {
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            this.notEmpty.signal();
        } finally {
            takeLock.unlock();
        }

    }

    private void signalNotFull() {
        ReentrantLock putLock = this.putLock;
        putLock.lock();

        try {
            this.notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    void fullyLock() {
        this.putLock.lock();
        this.takeLock.lock();
    }

    void fullyUnlock() {
        this.takeLock.unlock();
        this.putLock.unlock();
    }

    @Override
    public boolean isEmpty() {
        return count.get() == 0;
    }

    @Override
    public int size() {
        return this.count.get();
    }

    /**
     * 获取剩余容量
     *
     * @return
     */
    @Override
    public int remainingCapacity() {
        return this.capacity - this.count.get();
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return offer(e, false, timeout, unit);
    }

    /**
     * 阻塞等待加入核心队列
     *
     * @param e
     * @param isCore  是否是核心的
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean offer(E e, boolean isCore, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        } else {
            long nanos = unit.toNanos(timeout);
            ReentrantLock putLock = this.putLock;
            AtomicInteger count = this.count;
            putLock.lockInterruptibly();
            int c;
            try {
                while (count.get() == this.capacity) {
                    if (nanos <= 0L) {
                        return false;
                    }
                    nanos = this.notFull.awaitNanos(nanos);
                }
                if (isCore) {
                    core.enqueue(e);
                } else {
                    queue.enqueue(e);
                }
                c = count.getAndIncrement();
                if (c + 1 < this.capacity) {
                    this.notFull.signal();
                }
            } finally {
                putLock.unlock();
            }
            if (c == 0) {
                this.signalNotEmpty();
            }
            return true;
        }
    }

    @Override
    public boolean offer(E e) {
        return offer(e, false);
    }


    /**
     * 直接加入队列,如果加入失败,则返回false
     *
     * @param e
     * @param isCore 是否是核心队列
     * @return
     */
    @Override
    public boolean offer(E e, boolean isCore) {
        if (e == null) {
            throw new NullPointerException();
        } else {
            AtomicInteger count = this.count;
            if (count.get() == this.capacity) {
                return false;
            } else {
                ReentrantLock putLock = this.putLock;
                putLock.lock();
                int c;
                try {
                    if (count.get() == this.capacity) {
                        return false;
                    }
                    if (isCore) {
                        core.enqueue(e);
                    } else {
                        queue.enqueue(e);
                    }
                    c = count.getAndIncrement();
                    if (c + 1 < this.capacity) {
                        this.notFull.signal();
                    }
                } finally {
                    putLock.unlock();
                }

                if (c == 0) {
                    this.signalNotEmpty();
                }

                return true;
            }
        }
    }


    /**
     * 会阻塞线程等待有数据的取出来,同时把取出来的数据在队列中移除
     *
     * @return
     * @throws InterruptedException
     */
    @Override
    public E take() throws InterruptedException {
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        E x;
        int c;
        try {
            while (count.get() == 0) {
                this.notEmpty.await();
            }
            x = core.dequeue();
            if (x == null) {
                x = queue.dequeue();
            }
            c = count.getAndDecrement();
            if (c > 1) {
                this.notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }

        if (c == this.capacity) {
            this.signalNotFull();
        }

        return x;
    }

    /**
     * 等待一定的时长来阻塞线程取数据,如果超时则抛出异常,同时把取出来的数据在队列中移除
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();

        E x;
        int c;
        try {
            while (true) {
                if (count.get() != 0) {
                    x = core.dequeue();
                    if (x == null) {
                        x = queue.dequeue();
                    }
                    c = count.getAndDecrement();
                    if (c > 1) {
                        this.notEmpty.signal();
                    }
                    break;
                }

                if (nanos <= 0L) {
                    return null;
                }

                nanos = this.notEmpty.awaitNanos(nanos);
            }
        } finally {
            takeLock.unlock();
        }

        if (c == this.capacity) {
            this.signalNotFull();
        }

        return x;
    }

    /**
     * 不阻塞线程把数据取出来,如果没有数据则为null
     *
     * @return
     * @throws InterruptedException
     */
    @Override
    public E poll() {
        AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        } else {
            ReentrantLock takeLock = this.takeLock;
            takeLock.lock();

            E x;
            int c;
            try {
                if (count.get() == 0) {
                    return null;
                }
                x = core.dequeue();
                if (x == null) {
                    x = queue.dequeue();
                }
                c = count.getAndDecrement();
                if (c > 1) {
                    this.notEmpty.signal();
                }
            } finally {
                takeLock.unlock();
            }

            if (c == this.capacity) {
                this.signalNotFull();
            }

            return x;
        }
    }

    /**
     * 同时把取出来的数据在队列中移除,不会在队列中移除取出来的数据
     *
     * @return
     */
    @Override
    public E peek() {
        AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        } else {
            ReentrantLock takeLock = this.takeLock;
            takeLock.lock();

            E var3;
            try {
                var3 = core.peek();
                if (var3 == null) {
                    var3 = queue.peek();
                }
            } finally {
                takeLock.unlock();
            }

            return var3;
        }
    }

    /**
     * 在队列中移除
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        } else {
            this.fullyLock();
            boolean b = false;
            try {
                b = core.remove(o) || queue.remove(o);
            } finally {
                if (b) {
                    if (this.count.getAndDecrement() == this.capacity) {
                        this.notFull.signal();
                    }
                }
                this.fullyUnlock();
                return b;
            }
        }
    }

    /**
     * 判断是否包含
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        } else {
            this.fullyLock();

            try {
                return core.contains(o) || queue.contains(o);
            } finally {
                this.fullyUnlock();
            }
        }
    }

    @Override
    public Object[] toArray() {
        this.fullyLock();
        try {
            int size = this.count.get();
            Object[] var8 = new Object[size];
            int index = core.toArray(var8, 0);

            queue.toArray(var8, index);
            return var8;
        } finally {
            this.fullyUnlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.fullyLock();
        T[] var8;
        try {
            int size = this.count.get();
            if (a.length < size) {
                var8 = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            } else {
                var8 = a;
            }
            int index = core.toArray(var8, 0);
            queue.toArray(var8, index);
        } finally {
            this.fullyUnlock();
        }
        return var8;
    }

    /**
     * 清除所有
     */
    @Override
    public void clear() {
        this.fullyLock();
        try {
            core.clear();
            queue.clear();
            if (this.count.getAndSet(0) == this.capacity) {
                this.notFull.signal();
            }
        } finally {
            this.fullyUnlock();
        }

    }

    public int drainTo(Collection<? super E> c) {
        return this.drainTo(c, 2147483647);
    }


    /**
     * 把所有的队列数据移除
     *
     * @param c
     * @param maxElements
     * @return
     */
    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            boolean signalNotFull = false;
            ReentrantLock takeLock = this.takeLock;
            takeLock.lock();

            try {
                int n = Math.min(maxElements, this.count.get());
                int i = 0;
                try {
                    i = core.drainTo(c, n) + queue.drainTo(c, n);
                } finally {
                    if (i > 0) {
                        signalNotFull = this.count.getAndAdd(-i) == this.capacity;
                    }
                }
                return n;
            } finally {
                takeLock.unlock();
                if (signalNotFull) {
                    this.signalNotFull();
                }

            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private final AtomicInteger count = new AtomicInteger(0);
        private final Object[] array;

        Itr() {
            array = BinaryQueue.this.toArray();
        }

        public boolean hasNext() {
            return count.get() < array.length;
        }

        public E next() {
            final int index = count.getAndIncrement();
            if (index >= array.length) {
                throw new NoSuchElementException();
            } else {
                BinaryQueue.this.fullyLock();
                try {
                    E x = (E) array[index];
                    return x;
                } finally {
                    BinaryQueue.this.fullyUnlock();
                }
            }
        }

        public void remove() {
            final int index = count.get() - 1;
            if (index >= array.length) {
                throw new IllegalStateException();
            } else {
                BinaryQueue.this.fullyLock();
                try {
                    Object p = array[index];
                    BinaryQueue.this.remove(p);
                } finally {
                    BinaryQueue.this.fullyUnlock();
                }

            }
        }
    }
}
