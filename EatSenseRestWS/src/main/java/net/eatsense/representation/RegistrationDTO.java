package net.eatsense.representation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.bval.constraints.Email;
import org.apache.bval.constraints.NotEmpty;

public class RegistrationDTO {
	@NotNull
	@NotEmpty
	@Size(min = 3)
	String name;
	
	/**
	 * Login name must be between 4 and 30 characters with only lowercase letters, numbers, undercore, hyphen and dot.
	 */
	@NotNull
	@Size(min = 4, max = 30)
	@Pattern(regexp = "^[a-z0-9_\\.-]{4,30}$")
	String login;
	
	@NotNull
	@Email
	String email;
	
	String facebookUID;
	String facebookToken;
	
	/**
	 * Password pattern matches a password with at least one number, one symbolic and one unicode
	 */
	@NotNull
	@Size(min = 6)
	@Pattern(regexp= "^(?=[!-~]{6,}$)(?=.*\\d)(?=.*[^A-Za-z0-9]).*$")
	String password;
	
	String phone;
	
	@Valid
	CompanyDTO company;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getFacebookToken() {
		return facebookToken;
	}
	public void setFacebookToken(String facebookToken) {
		this.facebookToken = facebookToken;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getFacebookUID() {
		return facebookUID;
	}
	public void setFacebookUID(String facebookUID) {
		this.facebookUID = facebookUID;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public CompanyDTO getCompany() {
		return company;
	}
	public void setCompany(CompanyDTO company) {
		this.company = company;
	}
}
