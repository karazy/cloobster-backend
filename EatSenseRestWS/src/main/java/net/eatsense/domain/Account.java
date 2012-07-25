package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.UploadToken;
import net.eatsense.representation.ImageUploadDTO;

import org.apache.bval.constraints.Email;
import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class Account extends GenericEntity<Account> {
	@NotNull
	@NotEmpty
	String login;
	String hashedPassword;
	
	@NotNull 
	@Email
	String email;
	
	@Unindexed
	private String newEmail;
	
	@NotNull
	@NotEmpty
	String role;
	
	Date lastFailedLogin;
	int failedLoginAttempts;
	
	List<Key<Business>> businesses;
	
	@NotNull
	@NotEmpty
	String name;
	
	private String phone;
		
	@NotNull
	Key<Company> company;
	
	@Embedded
	private UploadToken uploadToken;
	
	@Embedded
	private List<ImageUploadDTO> imageUploads;
	
	private String facebookUid;
	private boolean emailConfirmed = false;
	private boolean active = false;
	
	private Date creationDate;
	
	public String getFacebookUid() {
		return facebookUid;
	}

	public void setFacebookUid(String facebookUid) {
		if(!Objects.equal(this.facebookUid, facebookUid)) {
			this.setDirty(true);
			this.facebookUid = facebookUid;
		}
	}

	public boolean isEmailConfirmed() {
		return emailConfirmed;
	}

	public void setEmailConfirmed(boolean emailConfirmed) {
		if(!Objects.equal(this.emailConfirmed, emailConfirmed)) {
			this.setDirty(true);
			this.emailConfirmed = emailConfirmed;
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if(!Objects.equal(this.active, active)) {
			this.setDirty(true);
			this.active = active;
		}
	}

	public Key<Company> getCompany() {
		return company;
	}

	public void setCompany(Key<Company> company) {
		if(!Objects.equal(this.company, company)) {
			this.setDirty(true);
			this.company = company;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}

	public List<Key<Business>> getBusinesses() {
		return businesses;
	}

	public void setBusinesses(List<Key<Business>> businesses) {
		if(!Objects.equal(this.businesses, businesses)) {
			this.setDirty(true);
			this.businesses = businesses;
		}
	}

	public Date getLastFailedLogin() {
		return lastFailedLogin;
	}

	public void setLastFailedLogin(Date lastFailedLogin) {
		if(!Objects.equal(this.lastFailedLogin, lastFailedLogin)) {
			this.setDirty(true);
			this.lastFailedLogin = lastFailedLogin;
		}
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		if(!Objects.equal(this.failedLoginAttempts, failedLoginAttempts)) {
			this.setDirty(true);
			this.failedLoginAttempts = failedLoginAttempts;
		}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		if(!Objects.equal(this.login, login)) {
			this.setDirty(true);
			this.login = login;
		}
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		if(!Objects.equal(this.hashedPassword, hashedPassword)) {
			this.setDirty(true);
			this.hashedPassword = hashedPassword;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if(!Objects.equal(this.email, email)) {
			this.setDirty(true);
			this.email = email;
		}
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		if(!Objects.equal(this.role, role)) {
			this.setDirty(true);
			this.role = role;
		}
	}
		
	@JsonIgnore
	@Transient
	public Key<Account> getKey() {
		return new Key<Account>(Account.class, super.getId());
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		if(!Objects.equal(this.phone, phone)) {
			this.setDirty(true);
			this.phone = phone;
		}
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public UploadToken getUploadToken() {
		return uploadToken;
	}

	public void setUploadToken(UploadToken uploadToken) {
		if(!Objects.equal(this.uploadToken, uploadToken)) {
			this.setDirty(true);
			this.uploadToken = uploadToken;
		}
	}

	public List<ImageUploadDTO> getImageUploads() {
		return imageUploads;
	}

	public void setImageUploads(List<ImageUploadDTO> imageUploads) {
		this.imageUploads = imageUploads;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}
}
