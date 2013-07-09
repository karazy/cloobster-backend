package net.eatsense;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;

public class ObjectifyModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}

	@Provides
	@Named("caching")
	public DatastoreService providesCachingDatastoreService() {
		return CachingDatastoreServiceFactory.getDatastoreService();
	}
}
