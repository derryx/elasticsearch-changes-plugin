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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Array;

public class ConcurrentCircularBuffer <T> {
    private final AtomicLong cursor = new AtomicLong();
    private final T[]      buffer;
    private final Class<T> type;

    public ConcurrentCircularBuffer (final Class <T> type, 
                                     final int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException(
                "Buffer size must be a positive value"
                );
        }

        this.type    = type;
        this.buffer = (T[]) Array.newInstance(type,  bufferSize);
    }

    public void add (final T sample) {
        buffer[(int) (cursor.getAndIncrement() % buffer.length)] = sample;
    }

    public List<T> snapshot () {
        T[] snapshot = (T[]) Array.newInstance(type, buffer.length);

        System.arraycopy(buffer, 0, snapshot, 0, buffer.length);
        
        // find last non-null entry
        int lastEntryIndex=-1;
        for (int i=snapshot.length-1;i>=0;--i) {
            if (snapshot[i]!=null) {
                lastEntryIndex=i;
                break;
            }
        }
        // buffer still empty?
        if (lastEntryIndex==-1) {
            return Collections.emptyList();
        }
        // buffer already full?
        if (lastEntryIndex==snapshot.length-1) {
            return Arrays.asList(snapshot);
        }
        
        T[] shortSnapshot=(T[])Array.newInstance(type, lastEntryIndex+1);
        System.arraycopy(snapshot, 0, shortSnapshot, 0, shortSnapshot.length);

        return Arrays.asList(shortSnapshot);
    }

    public int size () {
        return buffer.length;
    }
    
    public long getModificationCount() {
        return cursor.get();
    }
}