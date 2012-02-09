package org.elasticsearch.plugins.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugins.AbstractPlugin;

import org.elasticsearch.plugins.changes.module.ChangesModule;

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