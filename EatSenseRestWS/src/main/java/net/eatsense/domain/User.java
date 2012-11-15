/**
 * 
 */
package net.eatsense.domain;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;

/**
 * Represents a user during checkin, oders, etc. 
 * 
 * @author Nils Weiher
 *
 */
public class User extends GenericEntity<User> {
	
	
	/**
	 * User given name
	 */
	private String nickname;
	
	/**
	 * Unique UserId. Generated on checkIn if this user doesn't have a 
	 * user account.
	 */
	private String userId;
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@JsonIgnore
	@Transient
	public Key<User> getKey() {
		return new Key<User>(User.class, super.getId());
	}
}
