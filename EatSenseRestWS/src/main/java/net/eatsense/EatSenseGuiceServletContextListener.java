package net.eatsense;


import java.util.HashMap;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.restws.CronResource;
import net.eatsense.restws.NicknameResource;
import net.eatsense.restws.RestaurantResource;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
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
						bind(RestaurantResource.class);
						bind(NicknameResource.class);
						bind(CronResource.class);
						bind(Spot.class);
						bind(CheckIn.class);
						bind(Menu.class);
						bind(GenericRepository.class);
						serveRegex("(.)*restaurant(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*nickname(.)*").with(GuiceContainer.class, parameters);
						serveRegex("(.)*cron(.)*").with(GuiceContainer.class, parameters);
					}

				}, new ValidationModule());
	}

}
