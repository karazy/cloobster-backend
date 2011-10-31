package net.eatsense.persistence;

import java.util.Iterator;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.inject.Inject;
import com.googlecode.objectify.ObjectifyService;


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
		return null;
	}
	
	public Restaurant findByArea(String name) {
//		Iterator<Restaurant> result = datastore.find().type(Restaurant.class).addChildQuery().addFilter("name", FilterOperator.EQUAL, name).returnResultsNow();
//		return result.next();
		return null;
	}

}
