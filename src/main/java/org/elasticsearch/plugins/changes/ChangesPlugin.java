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
package org.elasticsearch.plugins.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugins.AbstractPlugin;

public class ChangesPlugin extends AbstractPlugin {
	private final ESLogger log;
	private final Collection<Class<? extends Module>> modules;
	
	public ChangesPlugin() {
		log=Loggers.getLogger(getClass());
		log.info("Starting ChangesPlugin");
		
		Collection<Class<? extends Module>> tempList=new ArrayList<Class<? extends Module>>();
		tempList.add(ChangesModule.class);
		modules=Collections.unmodifiableCollection(tempList);
	}
	
	@Override
	public Collection<Class<? extends Module>> modules() {
		return modules;
	}

	public String description() {
		return "Changes Plugin";
	}

	public String name() {
		return "changes";
	}
}