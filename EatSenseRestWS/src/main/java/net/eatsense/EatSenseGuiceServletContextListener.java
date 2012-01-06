package net.eatsense;


import org.apache.bval.guice.ValidationModule;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.restws.RestaurantResource;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
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
						bind(RestaurantResource.class);
						bind(Spot.class);
						bind(CheckIn.class);
						bind(Menu.class);
//						bind(RestaurantRepository.class);
						bind(GenericRepository.class);
//						bind(AreaRepository.class);
//						bind(CheckInRepository.class);
//						bind(BarcodeRepository.class);
//						bind(new TypeLiteral<GenericRepository<Restaurant>>(){}).to(new TypeLiteral<RestaurantRepository>(){});
//						bind(new TypeLiteral<GenericRepository<Area>>(){}).to(new TypeLiteral<AreaRepository>(){});
//						bind(new TypeLiteral<GenericRepository<Barcode>>(){}).to(new TypeLiteral<BarcodeRepository>(){});
//						bind(new TypeLiteral<GenericRepository<CheckIn>>(){}).to(new TypeLiteral<CheckInRepository>(){});
						//bind(ObjectifyService.class);
						// Route all requests through GuiceContainer
						// "(.)*restaurant(.)*"
						serveRegex("(.)*restaurant(.)*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));
						
					}

				}, new ValidationModule());
	}

}
