package com.x.thread.thread;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public interface ThreadQueue<E> {
    boolean isEmpty();

    int size();

    /**
     * 获取剩余容量
     *
     * @return
     */
    int remainingCapacity();

    /**
     * 阻塞等待加入核心队列
     *
     * @param e
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 阻塞等待加入核心队列
     *
     * @param e
     * @param isCore 是否是核心的
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    boolean offer(E e, boolean isCore, long timeout, TimeUnit unit) throws InterruptedException;


    /**
     * 直接加入队列,如果加入失败,则返回false
     *
     * @param e
     * @return
     */
    boolean offer(E e);

    /**
     * 直接加入队列,如果加入失败,则返回false
     *
     * @param e
     * @param isCore  是否是核心队列
     * @return
     */
    boolean offer(E e, boolean isCore);

    /**
     * 会阻塞线程等待有数据的取出来,同时把取出来的数据在队列中移除
     *
     * @return
     * @throws InterruptedException
     */
    E take() throws InterruptedException;

    /**
     * 等待一定的时长来阻塞线程取数据,如果超时则抛出异常,同时把取出来的数据在队列中移除
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 不阻塞线程把数据取出来,如果没有数据则为null
     *
     * @return
     * @throws InterruptedException
     */
    E poll();

    /**
     * 同时把取出来的数据在队列中移除,不会在队列中移除取出来的数据
     *
     * @return
     */
    E peek();

    /**
     * 在队列中移除
     */
    boolean remove(Object o);

    /**
     * 判断是否包含
     *
     * @param o
     * @return
     */
    boolean contains(Object o);

    Object[] toArray();

    <T> T[] toArray(T[] a);

    /**
     * 清除所有
     */
    void clear();

    int drainTo(Collection<? super E> c);

    /**
     * 把所有的队列数据移除
     *
     * @param c
     * @param maxElements
     * @return
     */
    int drainTo(Collection<? super E> c, int maxElements);

    Iterator<E> iterator();
}
