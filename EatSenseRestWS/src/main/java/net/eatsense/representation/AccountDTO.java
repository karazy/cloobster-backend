package net.eatsense.representation;

import net.eatsense.domain.Account;

public class AccountDTO {
	String login;
	String email;
	String passwordHash;
	String role;
	
	private Long companyId;
	private Long id;
	private String name;
	private String phone;
		
	public AccountDTO() {
		super();
	}
	
	/**
	 * Construct a new AccountDTO from the given Account entity.
	 * 
	 * @param account
	 */
	public AccountDTO(Account account) {
		super();
		if(account == null)
			return;
		
		this.id = account.getId();
		this.login = account.getLogin();
		this.email = account.getEmail();
		this.setName(account.getName());
		this.setPhone(account.getPhone());
		//TODO: Do not automatically return the password hash.
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
