package com.x.thread.thread;

import java.util.Collection;

final class QueueNode<E> {
    transient Node<E> head;
    transient Node<E> last;

    public void init() {
        this.last = this.head = new Node(null);
    }

    public void enqueue(E e) {
        Node<E> node = new Node<>(e);
        this.last = this.last.next = node;
    }

    public E dequeue() {
        Node<E> h = this.head;
        Node<E> first = h.next;
        if (first == null) return null;
        h.next = h;
        this.head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    public E peek() {
        if (this.head.next != null) {
            return this.head.next.item;
        } else {
            return null;
        }
    }

    public void unlink(Node<E> p, Node<E> pred) {
        p.item = null;
        pred.next = p.next;
        if (this.last == p) {
            this.last = pred;
        }
    }

    public boolean remove(Object o) {
        if (o != null) {
            Node<E> pred = this.head;
            for (Node<E> p = pred.next; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    this.unlink(p, pred);
                    return true;
                }

                pred = p;
            }
        }
        return false;
    }

    public boolean contains(Object o) {
        for (Node<E> p = this.head.next; p != null; p = p.next) {
            if (o.equals(p.item)) {
                return true;
            }
        }
        return false;
    }

    public int toArray(Object[] array, int k) {
        for (Node<E> p = this.head.next; p != null; p = p.next) {
            array[k++] = p.item;
        }
        return k;
    }

    public void clear() {
        this.last = this.head = new Node(null);
    }

    public int drainTo(Collection<? super E> c, int n) {
        Node<E> h = this.head;
        int i = 0;
        try {
            while (i < n) {
                Node<E> p = h.next;
                if (p == null) {
                    break;
                }
                c.add(p.item);
                p.item = null;
                h.next = h;
                h = p;
                ++i;
            }
        } finally {
            if (i > 0) {
                this.head = h;
            }
            return i;
        }
    }

    public Node<E> succ(Node<E> p) {
        if (p == (p = p.next)) {
            p = this.head.next;
        }

        return p;
    }

    Node<E> findPred(Node<E> p, Node<E> ancestor) {
        if (ancestor.item == null) {
            ancestor = this.head;
        }
        Node<E> q;
        while ((q = ancestor.next) != p) {
            ancestor = q;
        }
        return ancestor;
    }

    static class Node<E> {
        E item;
        Node<E> next;

        Node(E x) {
            this.item = x;
        }
    }
}
