package net.eatsense.representation;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;

public class AccountDTO {
	private String login;
	private String email;
	private String passwordHash;
	private String role;
	
	private Long companyId;
	private Long id;
	private String name;
	private String phone;
	
	private List<Long> businessIds;

		
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
		
		this.role = account.getRole();
		if(account.getCompany() != null)
			this.companyId = account.getCompany().getId();
		
		if(account.getBusinesses()!=null) {
			businessIds = new ArrayList<Long>();
			for (Key<Business> businessKey : account.getBusinesses()) {
				businessIds.add(businessKey.getId());
			}
		}
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
	
	public List<Long> getBusinessIds() {
		return businessIds;
	}

	public void setBusinessIds(List<Long> businessIds) {
		this.businessIds = businessIds;
	}
}
