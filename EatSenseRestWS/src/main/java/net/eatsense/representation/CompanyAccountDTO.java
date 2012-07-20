package net.eatsense.representation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import net.eatsense.domain.Account;
import net.eatsense.validation.CockpitUserChecks;
import net.eatsense.validation.PasswordChecks;

public class CompanyAccountDTO extends AccountDTO {
	/**
	 * Only supplied during account creation of a new cockpit user.
	 */
	/**
	 * Password pattern matches a password with at least one number or one unicode/symbolic and one alphabetical character.
	 */
	@Size(min = 6,groups={PasswordChecks.class})
	@Pattern(regexp= "^(?=[!-~]*$)(?=.*([^A-Za-z0-9]|\\d))(?=.*[a-zA-Z]).*$",groups={PasswordChecks.class})
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
