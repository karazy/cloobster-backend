package net.eatsense.domain;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.BarcodeType;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

/**
 * A users store card linked to a location.
 * 
 * @author Frederik Reifschneider
 *
 */
public class StoreCard extends GenericEntity<StoreCard> {
	
	@Transient
	private Key<StoreCard> key;
	
	/**
	 * Location this card is assigned to.
	 */
	@NotNull
	@NotEmpty
	private Key<Business> location;
	
	/**
	 * Account this card is assigned to.
	 */
	@NotNull
	@NotEmpty
	@Parent
	private Key<Account> account;
	
	/**
	 * The number card number identifying the owner.
	 */
	@NotNull
	@NotEmpty
	private String cardNumber;
	
	
	public StoreCard() {
		
	}

	/**
	 * @return the location
	 */
	public Key<Business> getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Key<Business> location) {
		this.location = location;
	}

	/**
	 * @return the account
	 */
	public Key<Account> getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Key<Account> account) {
		this.account = account;
	}

	/**
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * @param cardNumber the cardNumber to set
	 */
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	

	@Transient
	@JsonIgnore
	public Key<StoreCard> getKey() {
		if(key == null)
			this.key = getKey(super.getId());
		return key;
	}
	
	@Transient
	@JsonIgnore
	public static Key<StoreCard> getKey(Long id) {
		
		return new Key<StoreCard>(StoreCard.class, id);
	}	
	
	
	

}
