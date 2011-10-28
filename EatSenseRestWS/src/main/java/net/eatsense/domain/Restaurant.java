package net.eatsense.domain;

import java.util.List;

import com.vercer.engine.persist.annotation.Child;
import com.vercer.engine.persist.annotation.Embed;
import com.vercer.engine.persist.annotation.Key;
import com.vercer.engine.persist.annotation.Parent;


public class Restaurant {
	
	@Key
	private Long id;
	
	private String name;
	
	@Embed
	private Address address;
	
	private String description;
	
	private byte[] logo;
	
	@Child
	private List<Area> areas;

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
