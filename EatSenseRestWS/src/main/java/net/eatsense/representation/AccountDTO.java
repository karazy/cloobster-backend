package net.eatsense.representation;

import net.eatsense.domain.Account;

public class AccountDTO {
	String login;
	String email;
	String passwordHash;
	String role;
	
	private Long companyId;
	private Long id;
		
	public AccountDTO() {
		super();
	}
	
	public AccountDTO(Account account) {
		super();
		this.id = account.getId();
		this.login = account.getLogin();
		this.email = account.getEmail();
		this.passwordHash = account.getHashedPassword();
		this.role = account.getRole();
		if(account.getCompany() != null)
			this.companyId = account.getCompany().getId(); 
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
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
