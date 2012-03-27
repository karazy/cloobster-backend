package net.eatsense.representation;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.PaymentMethod;

import org.apache.bval.constraints.NotEmpty;



/**
 * Represents a pojo for data transfer of a location in e. g. a restaurant.
 * A spot is a place with a barcode where a user is able to check in.
 * 
 * @author Nils Weiher
 *
 */
public class SpotDTO {
	
	/**
	 * Barcode identifying this spot.
	 */
	@NotNull
	@NotEmpty
	private String barcode;
	
	/**
	 * A human readable identifier for the spot where the barcode is located.
	 * E.g. Table no. 4, Lounge etc.
	 */
	@NotNull
	@NotEmpty
	private String name;
	
	private String business;
	
	private Long businessId;
	
	private Collection<PaymentMethod> payments;
	
	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	/**
	 * A tag which can be used to group spots for easier organisation.
	 * E. g. Outside, Upper floor
	 */
	private String groupTag;


	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String code) {
		this.barcode = code;
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupTag() {
		return groupTag;
	}

	public void setGroupTag(String groupTag) {
		this.groupTag = groupTag;
	}

	public Collection<PaymentMethod> getPayments() {
		return payments;
	}

	public void setPayments(Collection<PaymentMethod> payments) {
		this.payments = payments;
	}
	
}
