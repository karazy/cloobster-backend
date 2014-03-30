package net.eatsense.representation;

import net.eatsense.domain.StoreCard;

/**
 * Representation of {@link net.eatsense.domain.StoreCard}.
 * @author Frederik Reifschneider
 *
 */
public class StoreCardDTO {
	
	/**
	 * Datastore key.
	 */
	private Long id;
	
	/**
	 * Number identifying the card in reality.
	 */
	private String cardNumber;
	
	/**
	 * Id of owner.
	 */
	private Long accountId;
	
	/**
	 * Location this card is assigned to.
	 */
	private Long locationId;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	
	
	
	/**
	 * @return the accountId
	 */
	public Long getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the locationId
	 */
	public Long getLocationId() {
		return locationId;
	}

	/**
	 * @param locationId the locationId to set
	 */
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public StoreCardDTO() {		
	}
	
	public StoreCardDTO(StoreCard sc) {
		super();
		
		if(sc == null) {
			return;
		}
		
		this.id = sc.getId();
		this.cardNumber = sc.getCardNumber();
		this.locationId = sc.getLocation().getId();
		this.accountId = sc.getAccount().getId();
	}
	
	
	
	
	

}
