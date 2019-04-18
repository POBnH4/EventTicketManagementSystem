package com.company;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class HistoryBuffer {
    // mutex lock and condition variables
    private Lock lock; // monitor entry lock
    private Condition bufferNotEmpty;
    private Condition bufferNotFull;
    // shared data that Monitor protects
    private Object[] buffer;
    private int bufferSize;
    private int in, out;
    private int numItems;

    HistoryBuffer(int size) {
        // initialise the lock and condition variables
        lock = new ReentrantLock();
        /* and initialise TWO condition variables
         * associated with the lock */
        bufferNotEmpty = lock.newCondition();
        bufferNotFull = lock.newCondition();
        bufferSize = size;
        in = 0;
        out = 0;
        numItems = 0;
        buffer = new Object[bufferSize];
    }

    // no need to synchronize monitor method
    // mutual exclusion is handled by the lock
    void add(Object x) {
        try {

            lock.lock();

            while (numItems == bufferSize)
                try {
                    bufferNotFull.await();
                } catch (InterruptedException ignored) {
                }

            buffer[in] = x;
            in = (in + 1) % bufferSize;
            numItems++;
            bufferNotEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // no need to synchronize monitor method
    // mutual exclusion is handled by the lock
    Object remove() {
        try {
            lock.lock();
            while (this.numItems == 0)
                try {
                    bufferNotEmpty.await();
                } catch (InterruptedException ignored) {
                }

            Object x = buffer[out];
            out = (out + 1) % bufferSize;
            numItems--;
            bufferNotFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}
