package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.googlecode.objectify.Key;



public class Restaurant {
	
	@Id
	private Long id;
	
	private String name;
	
	
	private String description;
	
	private byte[] logo;
	
	private List<Area> areas;
	
	public Restaurant() {
		this.areas = new ArrayList<Area>();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	public List<Area> getAreas() {
		return areas;
	}

	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}
	
	@Transient
	public Key<Restaurant> getKey() {
	   return new Key<Restaurant>(Restaurant.class, id);
	}
	
	

}
