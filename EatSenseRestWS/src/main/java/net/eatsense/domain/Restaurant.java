package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;


public class Restaurant extends GenericEntity {

	private String name;

	private String description;

	private byte[] logo;


	public Restaurant() {
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

	@Transient
	public Key<Restaurant> getKey() {
		return new Key<Restaurant>(Restaurant.class, super.getId());
	}

}
