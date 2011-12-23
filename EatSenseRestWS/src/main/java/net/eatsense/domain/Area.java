package net.eatsense.domain;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Area extends GenericEntity {

	private String name;

	@Parent
	private Key<Restaurant> restaurant;

	public Area() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
	}

}
