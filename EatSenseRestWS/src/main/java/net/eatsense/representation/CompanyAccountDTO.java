package net.eatsense.representation;

import net.eatsense.domain.Account;

public class CompanyAccountDTO extends AccountDTO {
	/**
	 * Only supplied during account creation of a new cockpit user.
	 */
	private String password;
	
	public CompanyAccountDTO() {
		super();
	}

	public CompanyAccountDTO(Account account) {
		super(account);

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
