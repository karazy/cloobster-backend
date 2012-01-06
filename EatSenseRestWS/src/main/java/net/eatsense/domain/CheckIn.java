package net.eatsense.domain;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import net.eatsense.domain.validation.CheckInStep2;

import com.googlecode.objectify.Key;

/**
 * Represents a user checked in on a certain spot in a restaurant.
 * Created when user tries to check in and exists until user payed.
 * Used to simulate the whole process of a restaurant visit.
 * 
 * @author Frederik Reifschneider
 *
 */
public class CheckIn extends GenericEntity{
		
	/**
	 * Unique UserId. Generated on checkIn if this user doesn't have a 
	 * user account.
	 */
	@NotNull
	private String userId;
	
	/**
	 * Status of this checkIn.
	 * {@link CheckInStatus}
	 */
	@NotNull
	private CheckInStatus status;
	
	/**
	 * A users nickname used for this checkin.
	 * E. g. Peter Pan or Funny Bee ;)
	 */
	@NotNull(groups=CheckInStep2.class)
	@Size(min = 3, max = 14, groups=CheckInStep2.class)
	private String nickname;
	
	/**
	 * Id of another user this checkIn is linked to.
	 * The other user can choose to pay for all users linked to him.
	 */
	private String linkedUserId;
	
	/**
	 * Restaurant this checkIn belongs to.
	 */
	@NotNull
	private Key<Restaurant> restaurant;
	
	/**
	 * Spot in this restaurant.
	 */
	@NotNull
	private Key<Spot> spot;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
	}

	public Key<Spot> getSpot() {
		return spot;
	}

	public void setSpot(Key<Spot> spot) {
		this.spot = spot;
	}

	public CheckInStatus getStatus() {
		return status;
	}

	public void setStatus(CheckInStatus status) {
		this.status = status;
	}
		
	@Transient
	public Key<CheckIn> getKey() {
		return new Key<CheckIn>(CheckIn.class, super.getId());
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getLinkedUserId() {
		return linkedUserId;
	}

	public void setLinkedUserId(String linkedUserId) {
		this.linkedUserId = linkedUserId;
	}
}
