package net.eatsense.domain;

import com.googlecode.objectify.Key;

public class CustomerProfile extends GenericEntity<CustomerProfile> {
	private String nickname;

	@Override
	public Key<CustomerProfile> getKey() {
		return new Key<CustomerProfile>(CustomerProfile.class, getId());
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
