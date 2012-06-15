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

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Frederik Reifschneider
 *
 */
@Cached
public class Business extends GenericEntity {
 
	/**
	 * Name of location.
	 */ 
	@NotNull
	@NotEmpty
	private String name;

	/**
	 * Description of location.
	 */
	private String description;
	/**
	 * Location's logo.
	 */
	private byte[] logo;

	@Embedded
	@Valid
	@Unindexed
	private List<PaymentMethod> paymentMethods;
	
	@Embedded
	private Set<Channel> channels;
	
	private String address;
	private String city;
	private String postcode;
	private String phone;
	
	private Key<Company> company;
	
	public String getAddress() {
		return address;
	}
	
	private Key<FeedbackForm> feedbackForm;

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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

	public Business() {
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
	public Key<Business> getKey() {
		
		return getKey(super.getId());
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
		this.paymentMethods = paymentMethods;
	}

	public Set<Channel> getChannels() {
		if(channels == null)
			channels = new HashSet<Channel>();
		return channels;
	}

	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}

	public Key<Company> getCompany() {
		return company;
	}

	public void setCompany(Key<Company> company) {
		this.company = company;
	}

	public Key<FeedbackForm> getFeedbackForm() {
		return feedbackForm;
	}

	public void setFeedbackForm(Key<FeedbackForm> feedbackForm) {
		this.feedbackForm = feedbackForm;
	}
	
	
}
