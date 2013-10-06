package net.eatsense.representation;

import net.eatsense.domain.Account;

/**
 * Holds only account data relevant for the business to deliver its service.
 * Such as fullfilling an order. Will not display sensitive account information like password.
 * This will only be accessible in {@link AccountDTO} for the owner!
 * 
 * @author Frederik Reifschneider
 *
 */
public class AccountForServiceDTO {
	
	private String email;
	
	public AccountForServiceDTO() {
		super();
	}
	
	public AccountForServiceDTO(Account account) {
		this();		
		
		this.email = account.getEmail();
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	

}
