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

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class ChangesAction extends BaseRestHandler {
	IndicesService indicesService;
	
	@Inject
	public ChangesAction(Settings settings, Client client, RestController controller, IndicesService indicesService) {
		super(settings, client);
		this.indicesService=indicesService;
	    controller.registerHandler(GET, "/_changes", this);
	    controller.registerHandler(GET, "/{index}/_changes", this);
	}

	@Override
	public void handleRequest(RestRequest request, RestChannel channel) {
		logger.debug("Request");

	}
}
