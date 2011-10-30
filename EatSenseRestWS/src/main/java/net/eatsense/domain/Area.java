package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.code.twig.annotation.Child;
import com.google.code.twig.annotation.Parent;



public class Area {
	
	private Long id;
	
	private String name;
	
	@Parent
	private Restaurant restaurant;
	
	@Child
	private List<Barcode> barcodes;

	public Area() {
		barcodes = new ArrayList<Barcode>();
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

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public List<Barcode> getBarcodes() {
		return barcodes;
	}

	public void setBarcodes(List<Barcode> barcodes) {
		this.barcodes = barcodes;
	}
	
	

}
	