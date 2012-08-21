package net.eatsense.representation;

import net.eatsense.domain.CustomerProfile;

public class CustomerProfileDTO {
	private String nickname;
	
	public CustomerProfileDTO() {
		super();
	}
	
	public CustomerProfileDTO(CustomerProfile profile) {
		this();
		if(profile == null)
			return;
		
		nickname = profile.getNickname();
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
}	
