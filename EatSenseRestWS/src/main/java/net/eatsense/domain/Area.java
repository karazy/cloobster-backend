package net.eatsense.domain;

import java.util.List;

import com.vercer.engine.persist.annotation.Child;
import com.vercer.engine.persist.annotation.Embed;
import com.vercer.engine.persist.annotation.Key;
import com.vercer.engine.persist.annotation.Parent;

public class Area {
	
	@Key
	private Long id;
	
	private String name;
	
	@Parent
	private Restaurant restaurant;
	
	@Embed
	private List<Barcode> barcodes;

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
	