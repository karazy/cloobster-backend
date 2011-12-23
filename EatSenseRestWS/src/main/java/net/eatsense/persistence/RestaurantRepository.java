package net.eatsense.persistence;

import net.eatsense.domain.Spot;
import net.eatsense.domain.Restaurant;

import com.googlecode.objectify.Query;


public class RestaurantRepository extends GenericRepository<Restaurant> {
	
	public RestaurantRepository() {
		super();
		super.clazz = Restaurant.class;
	}
	
	public Restaurant findByBarcode(String code) {
		Query<Spot> query = ofy().query(Spot.class).filter("barcode", code);		
		Spot bc = query.get();
		Restaurant restaurant = null;
		if(bc != null) {
			restaurant = ofy().find(bc.getRestaurant());
		}
			
		return restaurant;
	}
	
	
}
