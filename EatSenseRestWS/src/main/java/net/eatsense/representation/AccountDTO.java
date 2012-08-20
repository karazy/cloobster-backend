package net.eatsense.representation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import net.eatsense.domain.Account;
import net.eatsense.validation.CockpitUserChecks;
import net.eatsense.validation.EmailChecks;
import net.eatsense.validation.LoginNameChecks;

import org.apache.bval.constraints.Email;

public class AccountDTO {
	/**
	 * Login name must be between 4 and 30 characters with only lowercase letters, numbers, undercore, hyphen and dot.
	 */
	@NotNull(groups={CockpitUserChecks.class})
	@Size(min = 4, max = 30 ,groups={CockpitUserChecks.class, LoginNameChecks.class})
	@Pattern(regexp = "^[a-z0-9_\\.-]+$",groups={CockpitUserChecks.class,LoginNameChecks.class})
	private String login;
	
	@NotNull(groups={EmailChecks.class})
	@Email(groups={EmailChecks.class})
	private String email;
	private String newEmail;
	private boolean emailConfirmed;
	private String role;
	
	private Long id;
	private String name;
	private String accessToken;
	
	public AccountDTO() {
		super();
	}
	
	public AccountDTO(Account account) {
		this();
		if(account == null)
			return;
		
		this.id = account.getId();
		this.login = account.getLogin();
		this.email = account.getEmail();
		this.newEmail = account.getNewEmail();
		this.name = account.getName();
		this.role = account.getRole();
		this.emailConfirmed = account.isEmailConfirmed();
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNewEmail() {
		return newEmail;
	}
	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}
	public boolean isEmailConfirmed() {
		return emailConfirmed;
	}
	public void setEmailConfirmed(boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}	
}
