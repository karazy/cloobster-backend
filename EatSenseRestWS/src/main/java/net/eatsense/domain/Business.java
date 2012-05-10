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
	
	
}
