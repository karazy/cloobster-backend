package net.eatsense;


import java.util.HashMap;

import net.eatsense.auth.SecurityFilter;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.restws.AccountResource;
import net.eatsense.restws.ChannelResource;
import net.eatsense.restws.CheckInResource;
import net.eatsense.restws.CronResource;
import net.eatsense.restws.NicknameResource;
import net.eatsense.restws.RestaurantResource;
import net.eatsense.restws.SpotResource;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
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
		return Guice.createInjector(
				new JerseyServletModule() { 
					@Override 					
					protected void configureServlets() {
						HashMap<String, String> parameters = new HashMap<String, String>();
						parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
						parameters.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, SecurityFilter.class.getName());
						parameters.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
				                RolesAllowedResourceFilterFactory.class.getName());
						bind(RestaurantResource.class);
						bind(NicknameResource.class);
						bind(SpotResource.class);
						bind(CheckInResource.class);
						bind(CronResource.class);
						bind(AccountResource.class);
						bind(ChannelResource.class);
						bind(Spot.class);
						bind(CheckIn.class);
						bind(Menu.class);
						bind(GenericRepository.class);
						//serve("*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*accounts(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*restaurants(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*spots(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*checkins(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*nickname(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*_ah/channel/connected(.)*", "(.)*_ah/channel/disconnected(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*cron(.)*").with(GuiceContainer.class, parameters);
					}

				}, new ValidationModule());
	}

}
