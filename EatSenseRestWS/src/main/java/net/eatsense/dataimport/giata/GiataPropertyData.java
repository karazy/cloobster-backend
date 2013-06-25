package net.eatsense.dataimport.giata;

import com.google.appengine.api.datastore.GeoPt;

public class GiataPropertyData {
	private long giataId;
	private String name;
	private String address;
	private String city;
	private String postalCode;
	private String phone;
	private String email;
	private String url;
	private GeoPt geoCode;
	

	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	public long getGiataId() {
		return giataId;
	}
	public void setGiataId(long giataId) {
		this.giataId = giataId;
	}
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
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public GeoPt getGeoCode() {
		return geoCode;
	}
	public void setGeoCode(GeoPt geoCode) {
		this.geoCode = geoCode;
	}
}
