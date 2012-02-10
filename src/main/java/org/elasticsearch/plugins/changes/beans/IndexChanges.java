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
package org.elasticsearch.plugins.changes.beans;

import java.util.Arrays;
import java.util.Date;

import org.elasticsearch.plugins.changes.util.ConcurrentCircularBuffer;

public class IndexChanges {
	long lastChange;
    ConcurrentCircularBuffer<Change> changes;
	
	public IndexChanges(int capacity) {
		this.lastChange=System.currentTimeMillis();
		this.changes=new ConcurrentCircularBuffer<Change>(Change.class, capacity);
	}
	
	public void addChange(Change c) {
		lastChange=c.timestamp;
		changes.add(c);
	}
	
	public long getLastChangeMillis() {
		return lastChange;
	}
	
	public Date getLastChange() {
		return new Date(lastChange);
	}
	
	public Change[] getChanges() {
		Change[] snapshot=changes.completeSnapshot();
		
		Arrays.sort(snapshot);
		
		return snapshot;
	}
}
