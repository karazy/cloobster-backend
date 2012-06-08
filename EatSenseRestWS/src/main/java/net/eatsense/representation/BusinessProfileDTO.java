package net.eatsense.representation;

import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.PaymentMethod;

public class BusinessProfileDTO extends BusinessDTO {

	@NotNull
	@NotEmpty
	private String city;
	@NotEmpty
	@NotNull
	private String address;
	
	/**
	 * Maps to a json object images.{id}.
	 * Containing {@link Business#images}
	 */
	private LinkedHashMap<String,ImageDTO> images;
	private List<PaymentMethod> paymentMethods;
	private String phone;
	@NotEmpty
	@NotNull
	private String postcode;
	private String slogan;

	public BusinessProfileDTO() {
		super();
	}

	/**
	 * @param business - Business entity to copy the data from.
	 */
	public BusinessProfileDTO(Business business) {
		super(business);
		this.address = business.getAddress();
		this.city = business.getCity();
		// Convert the images List to a Map, use id as the key.
		// Useful so that we can access image objects directly by id in the JSON output.
		if(business.getImages() != null && !business.getImages().isEmpty()) {
			this.images = new LinkedHashMap<String, ImageDTO>();
			for(ImageDTO image : business.getImages()) {
				images.put(image.getId(), image);
			}
		}
		this.paymentMethods = business.getPaymentMethods();
		this.phone = business.getPhone();
		this.postcode = business.getPostcode();
		this.slogan = business.getSlogan();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LinkedHashMap<String, ImageDTO> getImages() {
		return images;
	}

	public void setImages(LinkedHashMap<String, ImageDTO> images) {
		this.images = images;
	}

	public List<PaymentMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}	
}
