package net.eatsense.representation;

import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

public class CompanyDTO {
	@NotNull
	@NotEmpty
	String name;
	@NotNull
	@NotEmpty
	String address;
	@NotNull
	@NotEmpty
	String city;
	@NotNull
	@NotEmpty
	String country;
	@NotNull
	@NotEmpty
	String postcode;
	
	String phone;
	
	String logoBlobKey;
	
	private LinkedHashMap<String,ImageDTO> images;
		
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
	public LinkedHashMap<String, ImageDTO> getImages() {
		return images;
	}
	public void setImages(LinkedHashMap<String, ImageDTO> images) {
		this.images = images;
	}
}
