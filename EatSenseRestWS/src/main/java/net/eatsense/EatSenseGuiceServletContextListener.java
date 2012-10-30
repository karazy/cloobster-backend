package net.eatsense;


import java.util.HashMap;

import net.eatsense.auth.AccessTokenFilter;
import net.eatsense.auth.AuthorizerFactory;
import net.eatsense.auth.AuthorizerFactoryImpl;
import net.eatsense.auth.SecurityFilter;
import net.eatsense.controller.MailController;
import net.eatsense.controller.MessageController;
import net.eatsense.exceptions.ServiceExceptionMapper;
import net.eatsense.restws.AccountResource;
import net.eatsense.restws.AdminResource;
import net.eatsense.restws.ChannelResource;
import net.eatsense.restws.CronResource;
import net.eatsense.restws.DownloadResource;
import net.eatsense.restws.NewsletterResource;
import net.eatsense.restws.NicknameResource;
import net.eatsense.restws.SpotResource;
import net.eatsense.restws.UploadsResource;
import net.eatsense.restws.business.AccountsResource;
import net.eatsense.restws.business.BusinessesResource;
import net.eatsense.restws.business.CompaniesResource;
import net.eatsense.restws.customer.CheckInsResource;
import net.eatsense.restws.customer.ProfilesResource;
import net.eatsense.util.NicknameGenerator;

import org.apache.bval.guice.ValidationModule;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
/**
 * Configures google guice for use in a servlet environment.
 * All condifured requests are send through the guice servlet.
 * 
 * @author Frederik Reifschneider
 *
 */
public class EatSenseGuiceServletContextListener extends
		GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(
				new JerseyServletModule() {
					@Override 		
					protected void configureServlets() {
						HashMap<String, String> parameters = new HashMap<String, String>();
						
						parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
						parameters.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
								AccessTokenFilter.class.getName() + ","+ SecurityFilter.class.getName());
						
						// add cross origin headers filter, deactivated for now.
						// add cache control response filter.
						parameters.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, CacheResponseFilter.class.getName());
						
						parameters.put(ResourceConfig.FEATURE_DISABLE_WADL, "true");
						
						parameters.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
				                RolesAllowedResourceFilterFactory.class.getName());
						bind(AccountsResource.class);
						bind(BusinessesResource.class);
						bind(net.eatsense.restws.customer.AccountsResource.class);
						bind(net.eatsense.restws.customer.BusinessesResource.class);
						bind(NicknameResource.class);
						bind(NewsletterResource.class);
						bind(SpotResource.class);
						bind(CheckInsResource.class);
						bind(CronResource.class);
						bind(AccountResource.class);
						bind(ChannelResource.class);
						bind(AdminResource.class);
						bind(EventBus.class).in(Singleton.class);
						bind(ServiceExceptionMapper.class);
						bind(NicknameGenerator.class);
						bind(UploadsResource.class);
						bind(DownloadResource.class);
						bind(CompaniesResource.class);
						bind(AuthorizerFactory.class).to(AuthorizerFactoryImpl.class);
						bind(ProfilesResource.class);
						
						//serve("*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*c/profiles(.)*",
								"(.)*c/accounts(.)*",
								"(.)*b/companies(.)*",
								"(.)*uploads(.)*",
								"(.)*b/accounts(.)*",
								"(.)*admin/services(.)*",
								"(.)*newsletter(.)*",
								"(.)*b/businesses(.)*",
								"(.)*c/businesses(.)*",
								"(.)*c/checkins(.)*",
								"(.)*accounts(.)*",
								"(.)*spots(.)*",
								"(.)*nickname(.)*",
								"(.)*download(.)*",
								"(.)*_ah/channel/connected(.)*",
								"(.)*_ah/channel/disconnected(.)*",								
								"(.)*cron(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*b/businesses(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*c/businesses(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*c/checkins(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*accounts(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*spots(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*nickname(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*_ah/channel/connected(.)*", "(.)*_ah/channel/disconnected(.)*").with(GuiceContainer.class, parameters);
//						serveRegex("(.)*cron(.)*").with(GuiceContainer.class, parameters);
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
					
					@Provides
					public FileService providesFileService() {
						return FileServiceFactory.getFileService();
					}
					
				}, new ValidationModule());
		// Register event listeners
		EventBus eventBus = injector.getInstance(EventBus.class);
		
		eventBus.register(injector.getInstance(MessageController.class));
		eventBus.register(injector.getInstance(MailController.class));
		
		return injector;
	}

}
