package net.eatsense;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import net.eatsense.auth.AccessTokenFilter;
import net.eatsense.auth.AuthorizerFactory;
import net.eatsense.auth.AuthorizerFactoryImpl;
import net.eatsense.auth.SecurityFilter;
import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.ConfigurationProvider;
import net.eatsense.configuration.WhiteLabelConfiguration;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.configuration.addon.AddonConfigurationServiceImpl;
import net.eatsense.controller.*;
import net.eatsense.exceptions.CapabilityDisabledExceptionMapper;
import net.eatsense.exceptions.ServiceExceptionMapper;
import net.eatsense.filter.ApiVersionFilterFactory;
import net.eatsense.filter.CacheResponseFilter;
import net.eatsense.filter.SuffixFilter;
import net.eatsense.persistence.OfyService;
import net.eatsense.restws.*;
import net.eatsense.restws.administration.AdminResource;
import net.eatsense.restws.business.AccountsResource;
import net.eatsense.restws.business.CompaniesResource;
import net.eatsense.restws.business.LocationsResource;
import net.eatsense.restws.business.SubscriptionTemplatesResource;
import net.eatsense.restws.customer.CheckInsResource;
import net.eatsense.restws.customer.ProfilesResource;
import net.eatsense.restws.customer.VisitsResource;
import net.eatsense.search.LocationSearchService;
import net.eatsense.util.NicknameGenerator;
import org.apache.bval.guice.ValidationModule;

import java.util.HashMap;

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
				bind(ActionResource.class);
        bind(LocationSearchService.class).in(Singleton.class);

				// Create Configuration binding to automatically load
				// configuration if needed.
				bind(Configuration.class).toProvider(ConfigurationProvider.class);
				
				bind(WhiteLabelConfiguration.class);

				// Add binding for counter task queue
				bind(Queue.class).annotatedWith(Names.named("counter-writebacks")).toInstance(
						QueueFactory.getQueue("counter-writebacks"));

				bind(AddonConfigurationService.class).to(AddonConfigurationServiceImpl.class);

				// serve("*").with(GuiceContainer.class, parameters);
				serveRegex("(.)*c/visits(.)*", "(.)*tasks/counter(.)*", "(.)*b/subscriptions(.)*",
						"(.)*c/profiles(.)*", "(.)*c/accounts(.)*", "(.)*b/companies(.)*", "(.)*uploads(.)*",
						"(.)*b/accounts(.)*", "(.)*admin/user(.)*", "(.)*admin/m(.)*", "(.)*admin/s(.)*",
						"(.)*newsletter(.)*", "(.)*b/businesses(.)*", "(.)*c/businesses(.)*", "(.)*c/checkins(.)*",
						"(.)*accounts(.)*", "(.)*spots(.)*", "(.)*nickname(.)*", "(.)*download(.)*","(.)*x(.)*",
						"(.)*_ah/channel/connected(.)*", "(.)*_ah/channel/disconnected(.)*", "(.)*cron(.)*").with(
						GuiceContainer.class, parameters);
			}


		}, new ValidationModule(), new AppEngineServiceModule(), new HtmlSanitizerModule(), new ObjectifyModule());

		// Register event listeners
		EventBus eventBus = injector.getInstance(EventBus.class);

		eventBus.register(injector.getInstance(SubscriptionController.class));
		eventBus.register(injector.getInstance(DashboardController.class));
		eventBus.register(injector.getInstance(MessageController.class));
		eventBus.register(injector.getInstance(MailController.class));
		eventBus.register(injector.getInstance(InfoPageController.class));
		eventBus.register(injector.getInstance(CounterController.class));
		eventBus.register(injector.getInstance(CheckInController.class));
    eventBus.register(injector.getInstance(LocationSearchService.class));

		// Register Objectify datastore entities.

		OfyService.registerEntities();

		return injector;
	}
}
