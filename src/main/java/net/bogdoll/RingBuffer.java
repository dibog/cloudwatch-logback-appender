package net.bogdoll;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class RingBuffer<E> {
    public final Lock lock = new ReentrantLock();
    private final Condition empty = lock.newCondition();
    private final AtomicInteger skipped = new AtomicInteger(0);
    private final int[] result = new int[2];

    private final E[] buffer;
    private final int cap;
    private int head = 0;
    private int size = 0;

    public RingBuffer(int aCapacity) {
        if(aCapacity<=0) throw new IllegalArgumentException("Capacity must be positive");
        cap = aCapacity;
        buffer = (E[])new Object[aCapacity];
    }

    /** Adds an item to the ring buffer.
     *
     * @param aElement the item to be added
     *
     * @return true if an item was overwritten
     * @return false if the item could be inserted without removing an old one
     *
     */
    public boolean put(E aElement) {
        lock.lock();

        try {
            E prev = buffer[head];
            buffer[head] = aElement;

            head++;
            if(head>=cap) {
                head -= cap;
            }

            size++;
            if(size>cap) {
                size = cap;
            }

            empty.signalAll();

            boolean overwritten = prev!=null;
            if(overwritten) {
                skipped.incrementAndGet();
            }
            return overwritten;
        }
        finally {
            lock.unlock();
        }
    }

    private int inc(int current) {
        assert 0<=current;

        int next = current+1;
        if(next>=cap) {
            next = next % cap;
        }

        return next;
    }

    /** Moves all items of the queue into the collection (FIFO).
     *
     * @param aCollection the collection into which the items are moved.
     *
     * @return (nbOfMessageCollected, nbOfSkippedMessages)
     */
    public int[] drainTo(Collection<E> aCollection) throws InterruptedException {
        lock.lock();
        try {

            while(size<=0) {
                empty.await();
            }

            final int base = head-size;
            for(int i=0; i<size; ++i) {
                int index = base+i;

                if(index<0) {
                    index += cap;
                }
                else if(index>=cap) {
                    index -= cap;
                }

                E elem = buffer[index];
                assert elem!=null;

                buffer[index]=null;

                aCollection.add(elem);
            }

            result[0] = size;
            result[1] = skipped.getAndSet(0);
            size = 0;

            return result;
        }
        finally {
            lock.unlock();
        }
    }
}
