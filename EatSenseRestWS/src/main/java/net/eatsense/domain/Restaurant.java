package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.code.twig.annotation.Child;
import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Entity;
import com.google.code.twig.annotation.Id;



@Entity(allocateIdsBy=10)
public class Restaurant {
	
	@Id
	private Long id;
	
	private String name;
	
	@Embedded
	private Address address;
	
	private String description;
	
	private byte[] logo;
	
	@Child
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

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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
	
	

}
