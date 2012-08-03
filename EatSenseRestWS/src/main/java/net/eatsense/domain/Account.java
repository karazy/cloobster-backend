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

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.NotSaved;

@Cached
public class Account extends GenericEntity<Account> {
	@NotNull
	@NotEmpty
	String login;
	String hashedPassword;
	
	@NotNull
	@Email
	String email;
	
	@NotNull
	@NotEmpty
	String role;
	
	Date lastFailedLogin;
	int failedLoginAttempts;
	
	/**
	 * (DEPRECATED) We keep this because we got a typo in this field.
	 */
	@NotSaved
	List<Key<Business>> businessess;
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
	private String emailConfirmationHash;
	private boolean emailConfirmed = false;
	private boolean active = false;
	
	private Date creationDate;
	
	public String getFacebookUid() {
		return facebookUid;
	}

	public void setFacebookUid(String facebookUid) {
		this.facebookUid = facebookUid;
	}

	public boolean isEmailConfirmed() {
		return emailConfirmed;
	}

	public void setEmailConfirmed(boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Key<Company> getCompany() {
		return company;
	}

	public void setCompany(Key<Company> company) {
		this.company = company;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Key<Business>> getBusinesses() {
		//Temporary: Remove after all entities are converted.
		if(businessess != null && !businessess.isEmpty()) {
			businesses.addAll(businessess);
			businessess = null;
		}
		return businesses;
	}

	public void setBusinesses(List<Key<Business>> businesses) {
		this.businesses = businesses;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getEmailConfirmationHash() {
		return emailConfirmationHash;
	}

	public void setEmailConfirmationHash(String emailConfirmationHash) {
		this.emailConfirmationHash = emailConfirmationHash;
	}

	public UploadToken getUploadToken() {
		return uploadToken;
	}

	public void setUploadToken(UploadToken uploadToken) {
		this.uploadToken = uploadToken;
	}

	public List<ImageUploadDTO> getImageUploads() {
		return imageUploads;
	}

	public void setImageUploads(List<ImageUploadDTO> imageUploads) {
		this.imageUploads = imageUploads;
	}
}
