package net.eatsense.representation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;




/**
 * Represents information transferred after submitting a barcode for checkIn.
 * This class is intended only for representation and will NOT BE persisted.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInDTO {
	

	/**
	 * Status indicating for example if a restaurant with this code was found.
	 */
	private String status;

	/**
	 * Restaurants name
	 */
	private String restaurantName;
	
	/**
	 * Restaurants UID
	 */
	private Long restaurantId;

	/**
	 * Name of location inside the restaurant
	 */
	private String spot;

	/**
	 * The userId used to identify this user.
	 */
	private String userId;
	
	/**
	 * A users nickname used for this checkin.
	 * E. g. Peter Pan or Funny Bee ;)
	 */
	private String nickname;
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}


	public String getSpot() {
		return spot;
	}

	public void setSpot(String spot) {
		this.spot = spot;
	}

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

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}
}
