package net.eatsense.persistence;

import java.util.Collection;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import com.googlecode.objectify.Query;


public class RestaurantRepository extends GenericRepository<Restaurant> {
	
	public RestaurantRepository() {
		super();
		super.clazz = Restaurant.class;
	}
	
	public Restaurant findByBarcode(String code) {
		Query<Barcode> query = ofy().query(Barcode.class).filter("barcode", code);		
		Barcode bc = query.get();
		Area area = null;
		Restaurant restaurant = null;
		if(bc != null) {
			area = ofy().find(bc.getArea());
		}
		if(area != null) {
			restaurant = ofy().find(area.getRestaurant());
		}
			
		return restaurant;
	}
	
	
}
