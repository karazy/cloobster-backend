package net.eatsense;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.restws.RestaurantResource;

import com.google.inject.AbstractModule;
import com.googlecode.objectify.ObjectifyService;

public class EatSenseDomainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(RestaurantResource.class);
		bind(Area.class);
		bind(Barcode.class);
		bind(ObjectifyService.class);
	}

}
