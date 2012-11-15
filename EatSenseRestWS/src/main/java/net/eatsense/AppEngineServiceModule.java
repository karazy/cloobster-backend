package net.eatsense;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class AppEngineServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}
	
	@Provides
	public ChannelService providesChannelService() {
		return ChannelServiceFactory.getChannelService();
	}
	@Provides
	public URLFetchService providesURLFetchService() {
		return URLFetchServiceFactory.getURLFetchService();
	}
	@Provides
	public BlobstoreService providesBlobStoreService() {
		return BlobstoreServiceFactory.getBlobstoreService();
	}
	@Provides
	public ImagesService providesImagesService() {
		return ImagesServiceFactory.getImagesService();
	}
}
