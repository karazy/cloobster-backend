package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;

/**
 * Represents a user checked in on a certain spot in a restaurant.
 * Created when user tries to check in and exists until user payed.
 * 
 * @author Frederik Reifschneider
 *
 */
public class CheckIn extends GenericEntity{
		
	private String userId;
	
	private CheckInStatus status;
	
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

	public CheckInStatus getStatus() {
		return status;
	}

	public void setStatus(CheckInStatus status) {
		this.status = status;
	}
		
	@Transient
	public Key<Restaurant> getKey() {
		return new Key<Restaurant>(Restaurant.class, super.getId());
	}
	

}
