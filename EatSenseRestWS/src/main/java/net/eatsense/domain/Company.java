package net.eatsense.domain;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Holds information for the company of a specific customer account.
 * @author Nils Weiher
 *
 */
public class Company extends GenericEntity {
	String name;
	String address;
	String city;
	String country;
	String postcode;
	String phone;
	
	private BlobKey logo;
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public BlobKey getLogo() {
		return logo;
	}
	public void setLogo(BlobKey logo) {
		this.logo = logo;
	}	
}
