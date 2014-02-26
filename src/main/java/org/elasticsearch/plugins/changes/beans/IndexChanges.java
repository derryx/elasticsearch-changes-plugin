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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.index.engine.Engine.Create;
import org.elasticsearch.index.engine.Engine.Delete;
import org.elasticsearch.index.engine.Engine.Index;
import org.elasticsearch.index.indexing.IndexingOperationListener;
import org.elasticsearch.plugins.changes.beans.Change.Type;
import org.elasticsearch.plugins.changes.util.ConcurrentCircularBuffer;

public class IndexChanges extends IndexingOperationListener {
    String indexName;
    long lastChange;
    ConcurrentCircularBuffer<Change> changes;
    AtomicInteger shardCount;
    List<IndexChangeWatcher> watchers;
    
    public IndexChanges(String indexName, int capacity) {
        this.indexName=indexName;
        this.lastChange=System.currentTimeMillis();
        this.changes=new ConcurrentCircularBuffer<Change>(Change.class, capacity);
        this.shardCount=new AtomicInteger();
        this.watchers=new CopyOnWriteArrayList<IndexChangeWatcher>();
    }

    public void removeWatcher(IndexChangeWatcher watcher) {
        watchers.remove(watcher);
    }
    
    public void addWatcher(IndexChangeWatcher watcher) {
        watchers.add(watcher);
    }
    
    public void triggerWatchers() {
        triggerWatchers(new Change());
    }
    
    private void triggerWatchers(Change c) {
        for (IndexChangeWatcher watch : watchers) {
            watch.setIndexName(indexName);
            watch.setChange(c);
            watch.permit();
        }
    }

    public long getLastChangeMillis() {
        return lastChange;
    }
    
    public long getLastChange() {
        return lastChange;
    }
    
    public List<Change> getChanges() {
        List<Change> snapshot=changes.snapshot();
        
        return snapshot;
    }
    
    public int addShard() {
        return shardCount.incrementAndGet();
    }
    
    public int removeShard() {
        return shardCount.decrementAndGet();
    }
    
    protected void addChange(Change c) {
        lastChange=c.timestamp;
        changes.add(c);
        triggerWatchers(c);
    }
    
    @Override
    public void postCreate(Create create) {
        Change change=new Change();
        change.id=create.id();
        change.type=Type.CREATE;
        change.version=create.version();
        change.timestamp=System.currentTimeMillis();
        
        addChange(change);
    }

    @Override
    public void postDelete(Delete delete) {
        Change change=new Change();
        change.id=delete.id();
        change.type=Type.DELETE;
        change.version=delete.version();
        change.timestamp=System.currentTimeMillis();
        
        addChange(change);
    }

    @Override
    public void postIndex(Index index) {
        Change change=new Change();
        change.id=index.id();
        change.type=Type.INDEX;
        change.version=index.version();
        change.timestamp=System.currentTimeMillis();
        
        addChange(change);        
    }
}