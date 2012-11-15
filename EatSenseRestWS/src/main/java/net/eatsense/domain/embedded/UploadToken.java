package net.eatsense.domain.embedded;

import java.util.Date;

public class UploadToken {
	String token;
	Date creation;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
}
