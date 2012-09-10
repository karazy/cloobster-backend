package net.eatsense.domain;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;

/**
 * Holds additional data for a customer account.
 * 
 * @author Nils Weiher
 * 
 */
public class CustomerProfile extends GenericEntity<CustomerProfile> {
	
	/**
	 * Will be used for all checkins with this account.
	 */
	private String nickname;

	@Override
	public Key<CustomerProfile> getKey() {
		return new Key<CustomerProfile>(CustomerProfile.class, getId());
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		if(!Objects.equal(this.nickname, nickname)) {
			this.setDirty(true);
			this.nickname = nickname;
		}
	}

}
