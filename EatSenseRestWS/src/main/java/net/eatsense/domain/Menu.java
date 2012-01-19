package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Menu extends GenericEntity{
	
	private String title;
	
	private String description;
	
	@Parent
	private Key<Restaurant> restaurant;
	
	
	
	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}



	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
	}



	@Transient
	public Key<Menu> getKey() {
		return new Key<Menu>(getRestaurant(), Menu.class, super.getId());
	}
	
}
