package net.eatsense;

import java.util.HashMap;

import net.eatsense.auth.AccessTokenFilter;
import net.eatsense.auth.AuthorizerFactory;
import net.eatsense.auth.AuthorizerFactoryImpl;
import net.eatsense.auth.SecurityFilter;
import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.ConfigurationProvider;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.configuration.addon.AddonConfigurationServiceImpl;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.CounterController;
import net.eatsense.controller.DashboardController;
import net.eatsense.controller.InfoPageController;
import net.eatsense.controller.MailController;
import net.eatsense.controller.MessageController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.exceptions.CapabilityDisabledExceptionMapper;
import net.eatsense.exceptions.ServiceExceptionMapper;
import net.eatsense.filter.ApiVersionFilterFactory;
import net.eatsense.filter.CacheResponseFilter;
import net.eatsense.filter.SuffixFilter;
import net.eatsense.persistence.OfyService;
import net.eatsense.restws.AccountResource;
import net.eatsense.restws.ChannelResource;
import net.eatsense.restws.CounterTasksResource;
import net.eatsense.restws.CronResource;
import net.eatsense.restws.DownloadResource;
import net.eatsense.restws.NewsletterResource;
import net.eatsense.restws.NicknameResource;
import net.eatsense.restws.SpotResource;
import net.eatsense.restws.UploadsResource;
import net.eatsense.restws.administration.AdminResource;
import net.eatsense.restws.business.AccountsResource;
import net.eatsense.restws.business.CompaniesResource;
import net.eatsense.restws.business.LocationsResource;
import net.eatsense.restws.business.SubscriptionTemplatesResource;
import net.eatsense.restws.customer.CheckInsResource;
import net.eatsense.restws.customer.ProfilesResource;
import net.eatsense.restws.customer.VisitsResource;
import net.eatsense.util.NicknameGenerator;

import org.apache.bval.guice.ValidationModule;
import org.owasp.html.AttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.googlecode.objectify.cache.CachingDatastoreService;
import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Configures google guice for use in a servlet environment. All condifured
 * requests are send through the guice servlet.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class EatSenseGuiceServletContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				HashMap<String, String> parameters = new HashMap<String, String>();

				parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

				parameters.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, AccessTokenFilter.class.getName()
						+ "," + SecurityFilter.class.getName() + "," + SuffixFilter.class.getName());

				// add cache control response filter.
				parameters.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, CacheResponseFilter.class.getName());

				parameters.put(ResourceConfig.FEATURE_DISABLE_WADL, "true");

				parameters.put(
						ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
						RolesAllowedResourceFilterFactory.class.getName() + ","
								+ ApiVersionFilterFactory.class.getName());
				bind(AccountsResource.class);
				bind(LocationsResource.class);
				bind(net.eatsense.restws.customer.AccountsResource.class);
				bind(net.eatsense.restws.customer.LocationsResource.class);
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
				bind(CapabilityDisabledExceptionMapper.class);
				bind(NicknameGenerator.class);
				bind(UploadsResource.class);
				bind(DownloadResource.class);
				bind(CompaniesResource.class);
				bind(AuthorizerFactory.class).to(AuthorizerFactoryImpl.class);
				bind(ProfilesResource.class);
				bind(SubscriptionTemplatesResource.class);
				bind(CounterTasksResource.class);
				bind(VisitsResource.class);

				// Create Configuration binding to automatically load
				// configuration if needed.
				bind(Configuration.class).toProvider(ConfigurationProvider.class);

				// Add binding for counter task queue
				bind(Queue.class).annotatedWith(Names.named("counter-writebacks")).toInstance(
						QueueFactory.getQueue("counter-writebacks"));

				bind(AddonConfigurationService.class).to(AddonConfigurationServiceImpl.class);

				// serve("*").with(GuiceContainer.class, parameters);
				serveRegex("(.)*c/visits(.)*", "(.)*tasks/counter(.)*", "(.)*b/subscriptions(.)*",
						"(.)*c/profiles(.)*", "(.)*c/accounts(.)*", "(.)*b/companies(.)*", "(.)*uploads(.)*",
						"(.)*b/accounts(.)*", "(.)*admin/user(.)*", "(.)*admin/m(.)*", "(.)*admin/s(.)*",
						"(.)*newsletter(.)*", "(.)*b/businesses(.)*", "(.)*c/businesses(.)*", "(.)*c/checkins(.)*",
						"(.)*accounts(.)*", "(.)*spots(.)*", "(.)*nickname(.)*", "(.)*download(.)*",
						"(.)*_ah/channel/connected(.)*", "(.)*_ah/channel/disconnected(.)*", "(.)*cron(.)*").with(
						GuiceContainer.class, parameters);
			}


		}, new ValidationModule(), new AppEngineServiceModule(), new HtmlSanitizerModule());

		// Register event listeners
		EventBus eventBus = injector.getInstance(EventBus.class);

		eventBus.register(injector.getInstance(SubscriptionController.class));
		eventBus.register(injector.getInstance(DashboardController.class));
		eventBus.register(injector.getInstance(MessageController.class));
		eventBus.register(injector.getInstance(MailController.class));
		eventBus.register(injector.getInstance(InfoPageController.class));
		eventBus.register(injector.getInstance(CounterController.class));
		eventBus.register(injector.getInstance(CheckInController.class));

		// Register Objectify datastore entities.

		OfyService.registerEntities();

		return injector;
	}

}
