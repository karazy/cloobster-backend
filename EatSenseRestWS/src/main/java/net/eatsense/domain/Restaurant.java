package net.eatsense.domain;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;


/**
 * Represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Frederik Reifschneider
 *
 */
public class Restaurant extends GenericEntity {

	/**
	 * Name of location.
	 */
	private String name;

	/**
	 * Description of location.
	 */
	private String description;

	/**
	 * Location's logo.
	 */
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
	@JsonIgnore
	public Key<Restaurant> getKey() {
		
		return new Key<Restaurant>(Restaurant.class, super.getId());
	}

}
