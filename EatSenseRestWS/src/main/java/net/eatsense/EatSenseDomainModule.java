package net.eatsense;

import net.eatsense.domain.Spot;
import net.eatsense.restws.RestaurantResource;

import com.google.inject.AbstractModule;
import com.googlecode.objectify.ObjectifyService;

/**
 * Configuration for google guice.
 * Defines all necessary bindings and producers methods for complex objects.
 * 
 * CURRENTLY NOT USED
 * 
 * @author Frederik Reifschneider
 *
 */ 
public class EatSenseDomainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(RestaurantResource.class);
		bind(Spot.class);
		bind(ObjectifyService.class);
	}

}
