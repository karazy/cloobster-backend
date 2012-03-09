package net.eatsense.domain;

import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Frederik Reifschneider
 *
 */
public class Restaurant extends GenericEntity {
 
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

	public Restaurant() {
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
	public Key<Restaurant> getKey() {
		
		return getKey(super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<Restaurant> getKey(Long id) {
		
		return new Key<Restaurant>(Restaurant.class, id);
	}

	public List<PaymentMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}
	
}
