package net.eatsense.representation;

import java.util.LinkedHashMap;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.Company;

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
	
	String url;
	

	String phone;
	
	private LinkedHashMap<String,ImageDTO> images;
	private Long id;
	
	/**
	 * Create the data transfer object with data from the supplied entity.
	 * 
	 * @param company - The entity to use the data from.
	 */
	public CompanyDTO(Company company) {
		super();
		if(company == null)
			return;
		
		this.setAddress(company.getAddress());
		this.setCity(company.getCity());
		this.setCountry(company.getCountry());
		this.setId(company.getId());
		
		if(company.getImages() != null && !company.getImages().isEmpty()) {
			LinkedHashMap<String, ImageDTO> images = new LinkedHashMap<String, ImageDTO>();
			
			for(ImageDTO image : company.getImages()) {
				images.put(image.getId(), image);
			}
			this.setImages(images);
		}
		this.setName(company.getName());
		this.setPhone(company.getPhone());
		this.setPostcode(company.getPostcode());
		this.setUrl(company.getUrl());
	}
	
	public CompanyDTO() {
		super();
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
