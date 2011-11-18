package net.eatsense;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.restws.RestaurantResource;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.googlecode.objectify.ObjectifyService;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class EatSenseGuiceServletContextListener extends
		GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
				new JerseyServletModule() {
					@Override
					protected void configureServlets() {
						bind(RestaurantResource.class);
						bind(Area.class);
						bind(Barcode.class);
						bind(ObjectifyService.class);
						// Route all requests through GuiceContainer
						serve("/*").with(GuiceContainer.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

					}

				});
	}

}
