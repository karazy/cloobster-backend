package net.eatsense.persistence;

import java.util.Map;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;


public class RestaurantRepository extends Repository<Restaurant> {
	
	
	@Inject
	public RestaurantRepository(ObjectifyService datastore) {
		super(datastore);
	}
	
	public Restaurant findByBarcode(String code) {
//		Iterator<Area> itArea = datastore.find().type(Barcode.class).addFilter("code", FilterOperator.EQUAL, code).<Area>returnParents().now();
////		datastore.find().type();
//		while(itArea.hasNext()) {
//			System.out.println(itArea.next().getName());
//		}
//		Barcode result = datastore.find().type(Barcode.class).addFilter("code", FilterOperator.EQUAL, code).returnUnique().now();
//		return result.getArea().getRestaurant();
				//.addChildQuery().addChildQuery().addFilter("code", FilterOperator.EQUAL, code).returnResultsNow();
//		return result.next();
		
		Objectify oiy = datastore.begin();
		Query<Barcode> query = oiy.query(Barcode.class).filter("code", code);		
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
	
	public Restaurant findByArea(String name) {
//		Iterator<Restaurant> result = datastore.find().type(Restaurant.class).addChildQuery().addFilter("name", FilterOperator.EQUAL, name).returnResultsNow();
//		return result.next();
		return null;
	}

}
