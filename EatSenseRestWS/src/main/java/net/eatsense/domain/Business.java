package net.eatsense.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.Channel;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.representation.ImageDTO;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Frederik Reifschneider
 * @author Nils Weiher
 */
@Cached
public class Business extends GenericEntity<Business> {
 
	/**
	 * Name of location.
	 */ 
	@NotNull
	@NotEmpty
	private String name;
	
	private String theme = "default";

	/**
	 * Description of location.
	 */
	@NotNull
	@NotEmpty
	private String description;
	
	/**
	 * For marketing on front pages and links. (optional)
	 */
	private String slogan;
	
	@Embedded
	@Valid
	@Unindexed
	private List<PaymentMethod> paymentMethods;
	
	@Embedded
	private Set<Channel> channels;
	
	private Key<FeedbackForm> feedbackForm;
	
	@NotNull
	@NotEmpty
	private String address;
	@NotNull
	@NotEmpty
	private String city;
	@NotNull
	@NotEmpty
	private String postcode;
	
	/**
	 * Phone number to contact the location. (optional)
	 */
	private String phone;
	
	private Key<Company> company;
	
	/**
	 * List of images used by the Business, at the moment we use 'logo' and several scrapbook images.
	 */
	@Embedded
	private List<ImageDTO> images;

	@Transient
	private Key<Business> key;
	
	private String currency;
	
	/**
	 * Link to a website for this location (e.g. for facebook posts)
	 */
	private String url;
	
	public Business() {
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


	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if(!Objects.equal(this.description, description)) {
			this.setDirty(true);
			this.description = description;
		}
	}

	@Transient
	@JsonIgnore
	public Key<Business> getKey() {
		if(key == null)
			this.key = getKey(super.getId());
		return key;
	}
	
	@Transient
	@JsonIgnore
	public static Key<Business> getKey(Long id) {
		
		return new Key<Business>(Business.class, id);
	}

	public List<PaymentMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
		if(!Objects.equal(this.paymentMethods, paymentMethods)) {
			this.setDirty(true);
			this.paymentMethods = paymentMethods;
		}
	}

	public Set<Channel> getChannels() {
		if(channels == null)
			channels = new HashSet<Channel>();
		return channels;
	}

	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}

	public Key<FeedbackForm> getFeedbackForm() {
		return feedbackForm;
	}

	public void setFeedbackForm(Key<FeedbackForm> feedbackForm) {
		this.feedbackForm = feedbackForm;
	}
	public Key<Company> getCompany() {
		return company;
	}

	public void setCompany(Key<Company> company) {
		this.company = company;
	}

	public List<ImageDTO> getImages() {
		return images;
	}

	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		if(!Objects.equal(this.slogan, slogan)) {
			this.setDirty(true);
			this.slogan = slogan;
		}
	}

	public String getCurrency() {
		if(currency == null) {
			currency = "EUR";
		}
		return currency;
	}

	public void setCurrency(String currency) {
		if(!Objects.equal(this.currency, currency)) {
			this.setDirty(true);
			this.currency = currency;
		}
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		if(!Objects.equal(this.theme, theme)) {
			this.setDirty(true);
			this.theme = theme;
		}
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
}
