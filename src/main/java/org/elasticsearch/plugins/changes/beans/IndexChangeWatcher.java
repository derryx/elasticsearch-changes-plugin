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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class IndexChangeWatcher {
    String indexName;
    Change change;
    Semaphore barrier;
    long timeout;
    
    public IndexChangeWatcher() {
        this.barrier=new Semaphore(0);
        this.timeout=15*60*1000;
    }
    
    public IndexChangeWatcher(long timeout) {
        this.barrier=new Semaphore(0);
        this.timeout=timeout;
    }
    
    public void permit() {
        barrier.release();
    }
    
    public boolean aquire() throws InterruptedException {
        return barrier.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }
}
