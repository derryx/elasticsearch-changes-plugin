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
package org.elasticsearch.plugins.changes.service;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;

//import org.elasticsearch.plugins.changes.listener.FilterShardListener;

public class ChangesService {
	private static final ESLogger log=Loggers.getLogger(ChangesService.class);
	
	@Inject
	public ChangesService(Settings settings, IndicesService indicesService) {
//		log.info("Starting ChangesService");
//		IndicesLifecycle lifecycle=indicesService.indicesLifecycle();
//		for (String name : Config.getFilterNames()) {
//			log.info("Registering listener for filter {}", name);
//			FilterShardListener listener=new FilterShardListener(name, Config.getFilterIndices(name));
//			lifecycle.addListener(listener);
//		}
	}
}