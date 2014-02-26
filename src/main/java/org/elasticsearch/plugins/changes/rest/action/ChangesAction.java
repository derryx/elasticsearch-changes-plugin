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
package org.elasticsearch.plugins.changes.rest.action;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.shard.service.IndexShard;
import org.elasticsearch.indices.IndicesLifecycle.Listener;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.plugins.changes.beans.Change;
import org.elasticsearch.plugins.changes.beans.IndexChangeWatcher;
import org.elasticsearch.plugins.changes.beans.IndexChanges;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.StringRestResponse;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

public class ChangesAction extends BaseRestHandler {
    private static final ESLogger LOG = Loggers.getLogger(ChangesAction.class);
    private static final String SETTING_HISTORY_SIZE="changes.history.size";
    private IndicesService indicesService;
    private Map<String, IndexChanges> changes;

    @Inject
    public ChangesAction(Settings settings, Client client,
            RestController controller, IndicesService indicesService) {
        super(settings, client);
        this.indicesService = indicesService;
        this.changes = new ConcurrentHashMap<String, IndexChanges>();
        controller.registerHandler(GET, "/_changes", this);
        controller.registerHandler(GET, "/{index}/_changes", this);

        registerLifecycleHandler();
    }

    private void registerLifecycleHandler() {
        indicesService.indicesLifecycle().addListener(new Listener() {
            @Override
            public void afterIndexShardStarted(IndexShard indexShard) {
                if (indexShard.routingEntry().primary()) {
                    IndexChanges indexChanges;
                    synchronized (changes) {
                        indexChanges = changes.get(indexShard.shardId().index().name());
                        if (indexChanges == null) {
                            indexChanges = new IndexChanges(indexShard.shardId().index()
                                    .name(),settings.getAsInt(SETTING_HISTORY_SIZE, 100));
                            changes.put(indexShard.shardId().index().name(), indexChanges);
                        }
                    }
                    indexChanges.addShard();
                    indexShard.indexingService().addListener(indexChanges);
                }
            }

        });
    }

    @Override
    public void handleRequest(final RestRequest request,
            final RestChannel channel) {
        logger.debug("Request");

        List<String> indices = Arrays.asList(splitIndices(request.param("index")));

        if (indices.isEmpty()) {
            indices = new ArrayList<String>(changes.keySet());
        }

        boolean wait = request.paramAsBoolean("wait",Boolean.FALSE);
        long timeout = request.paramAsLong("timeout", 15*60*1000);

        try {
            // Wait for trigger
            if (wait) {
                IndexChangeWatcher watcher = addWatcher(indices,timeout);
                boolean changeDetected = watcher.aquire();
                removeWatcher(indices);
                if (!changeDetected) {
                    channel.sendResponse(new StringRestResponse(RestStatus.NOT_FOUND,
                            "No change detected during timeout interval"));
                    return;
                }
                if (watcher.getChange() == null || watcher.getChange().getType() == null) {
                    channel.sendResponse(new StringRestResponse(RestStatus.NOT_FOUND,
                            "No more shards available to trigger waiting watch"));
                    return;
                }
            }
            
            XContentBuilder builder = createXContentBuilderForChanges(request, indices);
            channel.sendResponse(new XContentRestResponse(request, OK, builder));
        } catch (Exception e) {
            LOG.error("Error while handling change REST action", e);
            try {
                channel.sendResponse(new XContentThrowableRestResponse(request, e));
            } catch (IOException e1) {
                LOG.error("Error while sending error response", e1);
            }
        }
    }

    private XContentBuilder createXContentBuilderForChanges(final RestRequest request, List<String> indices) {
        XContentBuilder builder = null;
        try {
            long since = request.paramAsLong("since", Long.MIN_VALUE);
            builder = RestXContentBuilder.restContentBuilder(request);
            builder.startObject();
            for (String indexName : indices) {
                IndexChanges indexChanges = changes.get(indexName);
                if (indexChanges == null) {
                    continue;
                }
                builder.startObject(indexName);
                builder.field("lastChange", indexChanges.getLastChange());
                List<Change> changesList = indexChanges.getChanges();
                builder.startArray("changes");

                for (Change change : changesList) {
                    if (change.getTimestamp() > since) {
                        builder.startObject();

                        builder.field("type", change.getType().toString());
                        builder.field("id", change.getId());
                        builder.field("timestamp", change.getTimestamp());
                        builder.field("version", change.getVersion());

                        builder.endObject();
                    }
                }

                builder.endArray();
                builder.endObject();
            }
            builder.endObject();
        } catch (Exception e) {
            LOG.error("Error while setting XContentBuilder from response", e);
        }
        return builder;
    }
    
    IndexChangeWatcher addWatcher(List<String> indices, long timeout) {
        IndexChangeWatcher watcher=new IndexChangeWatcher(timeout);
        
        for (String index : indices) {
            IndexChanges change=changes.get(index);
            if (change!=null) {
                change.addWatcher(watcher);
            }
        }
        
        return watcher;
    }
    
    void removeWatcher(List<String> indices) {
        IndexChangeWatcher watcher=new IndexChangeWatcher();
        
        for (String index : indices) {
            IndexChanges change=changes.get(index);
            if (change!=null) {
                change.removeWatcher(watcher);
            }
        }        
    }

    public static String[] splitIndices(String indices) {
        if (indices == null) {
            return Strings.EMPTY_ARRAY;
        }
        return Strings.splitStringByCommaToArray(indices);
    }
}