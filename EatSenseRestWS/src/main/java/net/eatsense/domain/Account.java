package net.eatsense.domain;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

@Cached
public class Account extends GenericEntity {
	String login;
	String securedPassword;
	String email;
	String role;
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSecuredPassword() {
		return securedPassword;
	}

	public void setSecuredPassword(String securedPassword) {
		this.securedPassword = securedPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@JsonIgnore
	@Transient
	public Key<Account> getKey() {
		return new Key<Account>(Account.class, super.getId());
	}
}
