package net.eatsense.representation;

import net.eatsense.domain.embedded.CheckInStatus;

/**
 * Represents information transferred after submitting a barcode for checkIn.
 * This class is intended only for representation and will NOT BE persisted.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInDTO {
	/**
	 * Status indicating for example if a customer checked in or an order was placed.
	 */
	private CheckInStatus status;

	/**
	 * business name
	 */
	private String businessName;
	
	/**
	 * business UID
	 */
	private Long businessId;

	/**
	 * Name of location inside the business
	 */
	private String spot;
	
	private String spotId;

	public String getSpotId() {
		return spotId;
	}

	public void setSpotId(String spotId) {
		this.spotId = spotId;
	}

	/**
	 * The userId used to identify this user.
	 */
	private String userId;
	
	private String linkedCheckInId;
	
	/**
	 * A users nickname used for this checkin.
	 * E. g. Peter Pan or Funny Bee ;)
	 */
	private String nickname;
	
	/**
	 * The unique Id of the phone. Primarily used to block users
	 * who try to abuse the service by issuing orders they don't
	 * pay or need.
	 */
	private String deviceId;
	
	private ErrorDTO error;
	

	public CheckInStatus getStatus() {
		return status;
	}

	public void setStatus(CheckInStatus status) {
		this.status = status;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
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

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ErrorDTO getError() {
		return error;
	}

	public void setError(ErrorDTO error) {
		this.error = error;
	}

	public String getLinkedCheckInId() {
		return linkedCheckInId;
	}

	public void setLinkedCheckInId(String linkedCheckInId) {
		this.linkedCheckInId = linkedCheckInId;
	}
	
	
}
