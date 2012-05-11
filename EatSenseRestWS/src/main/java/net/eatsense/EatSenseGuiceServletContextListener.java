package net.eatsense;


import java.util.HashMap;

import net.eatsense.auth.SecurityFilter;
import net.eatsense.controller.MessageController;
import net.eatsense.exceptions.ServiceExceptionMapper;
import net.eatsense.restws.AccountResource;
import net.eatsense.restws.ChannelResource;
import net.eatsense.restws.CronResource;
import net.eatsense.restws.NewsletterResource;
import net.eatsense.restws.NicknameResource;
import net.eatsense.restws.SpotResource;
import net.eatsense.restws.business.BusinessesResource;
import net.eatsense.restws.customer.CheckInsResource;

import org.apache.bval.guice.ValidationModule;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
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
						parameters.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, SecurityFilter.class.getName());
						parameters.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
				                RolesAllowedResourceFilterFactory.class.getName());
						bind(BusinessesResource.class);
						bind(net.eatsense.restws.customer.BusinessesResource.class);
						bind(NicknameResource.class);
						bind(NewsletterResource.class);
						bind(SpotResource.class);
						bind(CheckInsResource.class);
						bind(CronResource.class);
						bind(AccountResource.class);
						bind(ChannelResource.class);
						bind(EventBus.class).in(Singleton.class);
						bind(ServiceExceptionMapper.class);
						
						//serve("*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*newsletter(.)*", "(.)*b/businesses(.)*",
								"(.)*c/businesses(.)*","(.)*c/checkins(.)*",
								"(.)*accounts(.)*", "(.)*spots(.)*",
								"(.)*nickname(.)*", "(.)*_ah/channel/connected(.)*",
								"(.)*_ah/channel/disconnected(.)*", "(.)*cron(.)*").with(GuiceContainer.class, parameters);
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
				}, new ValidationModule());
		// Register event listeners
		EventBus eventBus = injector.getInstance(EventBus.class);
		
		eventBus.register(injector.getInstance(MessageController.class));
		
		return injector;
	}

}
