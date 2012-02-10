/*
   Copyright 2012 Thomas Peuss

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.elasticsearch.plugins.changes.util;

import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Array;

public class ConcurrentCircularBuffer <T> {
    private final AtomicLong cursor = new AtomicLong();
    private final T[]      buffer;
    private final Class<T> type;

    public ConcurrentCircularBuffer (final Class <T> type, 
                                     final int bufferSize) 
    {
        if (bufferSize < 1) {
            throw new IllegalArgumentException(
                "Buffer size must be a positive value"
                );
        }

        this.type    = type;
        this.buffer = (T[]) new Object [ bufferSize ];
    }

    public void add (T sample) {
        buffer[(int) (cursor.getAndIncrement() % buffer.length)] = sample;
    }

    public T[] snapshot () {
        T[] snapshots = (T[]) new Object [ buffer.length ];

        long before = cursor.get();

        if (before == 0) {
            return (T[]) Array.newInstance(type, 0);
        }

        System.arraycopy(buffer, 0, snapshots, 0, buffer.length);

        long after          = cursor.get();
        int  size           = buffer.length - (int) (after - before);
        long snapshotCursor = before - 1;

        if (size <= 0) {
            return (T[]) Array.newInstance(type, 0);
        }

        long start = snapshotCursor - (size - 1);
        long end   = snapshotCursor;

        if (snapshotCursor < snapshots.length) {
            size   = (int) snapshotCursor + 1;
            start  = 0;
        }

        T[] result = (T[]) Array.newInstance(type, size);

        int startOfCopy = (int) (start % snapshots.length);
        int endOfCopy   = (int) (end   % snapshots.length);

        if (startOfCopy > endOfCopy) {
            System.arraycopy(snapshots, startOfCopy,
                             result, 0, 
                             snapshots.length - startOfCopy);
            System.arraycopy(snapshots, 0,
                             result, (snapshots.length - startOfCopy),
                             endOfCopy + 1);
        }
        else {
            System.arraycopy(snapshots, startOfCopy,
                             result, 0, endOfCopy - startOfCopy + 1);
        }

        return (T[]) result;
    }

    public T[] completeSnapshot () {
        T[] snapshot = snapshot();

        while (snapshot.length != buffer.length) {
            snapshot = snapshot();
        }

        return snapshot;
    }

    public int size () {
        return buffer.length;
    }
}