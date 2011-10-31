package net.eatsense.domain;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;



public class Area {
	
	@Id
	private Long id;
	
	private String name;
	
	@Parent
	private Key<Restaurant> restaurant;
	
//	@Child
//	private List<Barcode> barcodes;

	public Area() {
//		barcodes = new ArrayList<Barcode>();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

//	public List<Barcode> getBarcodes() {
//		return barcodes;
//	}
//
//	public void setBarcodes(List<Barcode> barcodes) {
//		this.barcodes = barcodes;
//	}
	
	

}
	