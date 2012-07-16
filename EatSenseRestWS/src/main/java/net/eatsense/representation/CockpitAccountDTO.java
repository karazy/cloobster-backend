package net.eatsense.representation;

import net.eatsense.domain.Account;

public class CockpitAccountDTO extends AccountDTO {
	/**
	 * Only supplied during account creation of a new cockpit user.
	 */
	private String password;
	
	public CockpitAccountDTO() {
		super();
	}

	public CockpitAccountDTO(Account account) {
		super(account);

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
