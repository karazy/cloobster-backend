package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.Email;
import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

@Cached
public class Account extends GenericEntity {
	@NotNull
	@NotEmpty
	String login;
	String hashedPassword;
	@Email
	String email;
	
	@NotNull
	@NotEmpty
	String role;
	
	Date lastFailedLogin;
	int failedLoginAttempts;
	
	List<Key<Business>> businessess;
	
	public List<Key<Business>> getBusinesses() {
		return businessess;
	}

	public void setBusinesses(List<Key<Business>> businesses) {
		this.businessess = businesses;
	}

	public Date getLastFailedLogin() {
		return lastFailedLogin;
	}

	public void setLastFailedLogin(Date lastFailedLogin) {
		this.lastFailedLogin = lastFailedLogin;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
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
