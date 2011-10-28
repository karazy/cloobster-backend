package net.eatsense;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.vercer.engine.persist.ObjectDatastore;
import com.vercer.engine.persist.annotation.AnnotationObjectDatastore;

public class EatSenseDomainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ObjectDatastore.class).to(AnnotationObjectDatastore.class).in(Singleton.class);
	}

}
