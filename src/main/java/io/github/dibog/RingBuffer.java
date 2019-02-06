/*
 * Copyright 2018  Dieter Bogdoll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.dibog;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class RingBuffer<E> {
    public final Lock lock = new ReentrantLock();
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

    public int getSize() {
        return size;
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
    public int[] drainTo(Collection<E> aCollection) {
        lock.lock();
        try {
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
