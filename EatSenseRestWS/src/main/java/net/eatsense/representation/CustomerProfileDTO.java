package net.eatsense.representation;

import net.eatsense.domain.CustomerProfile;

public class CustomerProfileDTO {
	private long id;
	private String nickname;
	
	public CustomerProfileDTO() {
		super();
	}
	
	public CustomerProfileDTO(CustomerProfile profile) {
		this();
		if(profile == null)
			return;
		
		id = profile.getId();
		nickname = profile.getNickname();
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}	
