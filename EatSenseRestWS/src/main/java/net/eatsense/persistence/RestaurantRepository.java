package net.eatsense.persistence;

import net.eatsense.domain.Restaurant;

import com.google.inject.Inject;
import com.vercer.engine.persist.ObjectDatastore;

public class RestaurantRepository extends Repository<Restaurant> {

	@Inject
	public RestaurantRepository(ObjectDatastore datastore) {
		super(datastore);
	}

}
