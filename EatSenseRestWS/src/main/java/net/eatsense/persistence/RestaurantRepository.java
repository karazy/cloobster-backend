package net.eatsense.persistence;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;


public class RestaurantRepository extends GenericRepository<Restaurant> {
	
	
	@Inject
	public RestaurantRepository(ObjectifyService datastore) {
		super(datastore);
	}
	
	public Restaurant findByBarcode(String code) {
		Objectify oiy = datastore.begin();
		Query<Barcode> query = oiy.query(Barcode.class).filter("barcode", code);		
		Barcode bc = query.get();
		Area area = null;
		Restaurant restaurant = null;
		if(bc != null) {
			area = oiy.find(bc.getArea());
		}
		if(area != null) {
			restaurant = oiy.find(area.getRestaurant());
		}
			
		return restaurant;
	}
	
	
}
