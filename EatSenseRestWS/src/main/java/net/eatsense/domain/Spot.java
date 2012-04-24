package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;


/**
 * Represents a location in e. g. a restaurant.
 * A spot is a place with a barcode where a user is able to check in.
 * 
 * @author Frederik Reifschneider
 *
 */
public class Spot extends GenericEntity{
	
	/**
	 * The business this spot belongs to.
	 */
	@Parent
	private Key<Business> business;
	
	/**
	 * Barcode identifying this spot.
	 */
	private String barcode;
	
	/**
	 * A human readable identifier for the spot where the barcode is located.
	 * E.g. Table no. 4, Lounge etc.
	 */
	private String name;
	
	/**
	 * A tag which can be used to group spots for easier organisation.
	 * E. g. Outside, Upper floor
	 */
	private String groupTag;


	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String code) {
		this.barcode = code;
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		this.business = business;
	}

	public String getGroupTag() {
		return groupTag;
	}

	public void setGroupTag(String groupTag) {
		this.groupTag = groupTag;
	}
	
	@Transient
	public Key<Spot> getKey() {
	   return getKey(getBusiness(), getId());
	}
	
	@Transient
	public static Key<Spot> getKey(Key<Business> business, Long spotId) {
		return new Key<Spot>(business ,Spot.class,spotId);
	}
}
