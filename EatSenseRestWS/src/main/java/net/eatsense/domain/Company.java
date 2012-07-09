package net.eatsense.domain;

import java.util.List;

import javax.persistence.Embedded;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;

import net.eatsense.representation.ImageDTO;


/**
 * Holds information for the company of a specific customer account.
 * @author Nils Weiher
 *
 */
public class Company extends GenericEntity<Company> {
	String name;
	String address;
	String city;
	String country;
	String postcode;
	String phone;
	String url;
	
	@Embedded
	private List<ImageDTO> images;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		if(!Objects.equal(this.address, address)) {
			this.setDirty(true);
			this.address = address;
		}
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		if(!Objects.equal(this.city, city)) {
			this.setDirty(true);
			this.city = city;
		}
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		if(!Objects.equal(this.country, country)) {
			this.setDirty(true);
			this.country = country;
		}
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		if(!Objects.equal(this.postcode, postcode)) {
			this.setDirty(true);
			this.postcode = postcode;
		}
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		if(!Objects.equal(this.phone, phone)) {
			this.setDirty(true);
			this.phone = phone;
		}
	}
	public List<ImageDTO> getImages() {
		return images;
	}
	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		if(!Objects.equal(this.url, url)) {
			this.setDirty(true);
			this.url = url;
		}
	}
	@Override
	public Key<Company> getKey() {
		return getKey(getId());
	}
	
	public static Key<Company> getKey(long id) {
		return new Key<Company>(Company.class, id);
	}
}
