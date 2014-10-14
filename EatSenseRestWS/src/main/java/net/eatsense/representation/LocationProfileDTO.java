package net.eatsense.representation;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.constraints.Min;

import net.eatsense.domain.Business;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class LocationProfileDTO extends LocationDTO {

	private String city;
	private String address;
	private String postcode;
	
	/**
	 * Maps to a json object images.{id}.
	 * Containing {@link Business#images}
	 */
	private LinkedHashMap<String,ImageDTO> images;
	
	private String phone;

	private String slogan;
	
	private String email;
	
	@Min(0)
	private int stars;
	
	private Integer spotCount;
	
	private boolean offlineEmailAlertActive;
	
	private Map<String, Boolean> features = Maps.newHashMap();
	private Map<String, Map<String, String>> configuration;
	
	private Double distance;

	public LocationProfileDTO() {
		super();
	}

	/**
	 * @param business - Business entity to copy the data from.
	 */
	public LocationProfileDTO(Business business) {
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
		this.email = business.getEmail();
		this.setStars(business.getStars());
		this.phone = business.getPhone();
		this.postcode = business.getPostcode();
		this.slogan = business.getSlogan();
		this.spotCount = business.getSpotCount();
		this.offlineEmailAlertActive = business.isOfflineEmailAlertActive();
		
		for (String featureName : Business.AVAILABLE_FEATURES) {
			getFeatures().put(featureName, !business.getDisabledFeatures().contains(featureName));
		}
		for (String featureName : Business.AVAILABLE_OPTIONAL_FEATURES) {
			getFeatures().put(featureName, business.getEnabledOptionalFeatures().contains(featureName));
		}
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}
	
	public Integer getSpotCount() {
		return spotCount;
	}

	public void setSpotCount(Integer spotCount) {
		this.spotCount = spotCount;
	}

	public boolean isOfflineEmailAlertActive() {
		return offlineEmailAlertActive;
	}

	public void setOfflineEmailAlertActive(boolean offlineEmailAlertActive) {
		this.offlineEmailAlertActive = offlineEmailAlertActive;
	}

	public Map<String, Boolean> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, Boolean> features) {
		this.features = features;
	}

	public Map<String, Map<String, String>> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<String, Map<String, String>> configuration) {
		this.configuration = configuration;
	}		

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}



	public final static Function<Business, LocationProfileDTO> toDTO = 
			new Function<Business, LocationProfileDTO>() {
				@Override
				public LocationProfileDTO apply(Business input) {
					return new LocationProfileDTO(input);
				}
		    };
}
