package net.eatsense;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.restws.RestaurantResource;

import com.google.code.twig.ObjectDatastore;
import com.google.code.twig.annotation.AnnotationObjectDatastore;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
public class EatSenseDomainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ObjectDatastore.class).to(AnnotationObjectDatastore.class).in(Singleton.class);
		bind(RestaurantResource.class);
		bind(Area.class);
		bind(Barcode.class);
	}
}
