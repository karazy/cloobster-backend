package net.eatsense.domain;

import com.googlecode.objectify.Key;

/**
 * Represents a user checked in on a certain spot in a restaurant.
 * 
 * @author Frederik Reifschneider
 *
 */
public class CheckIn extends GenericEntity{
		
	private String userId;
	
	private Key<Restaurant> restaurant;
	
	private Key<Barcode> spot;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
	}

	public Key<Barcode> getSpot() {
		return spot;
	}

	public void setSpot(Key<Barcode> spot) {
		this.spot = spot;
	}
	
	

}
