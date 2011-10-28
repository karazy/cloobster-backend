package net.eatsense.persistence;

import net.eatsense.domain.Restaurant;

import com.vercer.engine.persist.ObjectDatastore;

public class RestaurantRepository extends Repository<Restaurant> {

	public RestaurantRepository(ObjectDatastore datastore) {
		super(datastore);
	}

}
