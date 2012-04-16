package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.validation.CheckInStep2;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

/**
 * Represents a user checked in on a certain spot in a location.
 * Created when user tries to check in and exists until user payed.
 * Used to simulate the whole process of a visit in a location.
 * 
 * @author Frederik Reifschneider
 *
 */
@Cached
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
	@Size(min = 3, max = 25, groups=CheckInStep2.class)
	private String nickname;
	
	/**
	 * Id of another user this checkIn is linked to.
	 * The other user can choose to pay for all users linked to him.
	 */
	private String linkedUserId;
	
	/**
	 * Time of checkin.
	 */
	private Date checkInTime;
	
	/**
	 * The unique Id of the phone. Primarily used to block users
	 * who try to abuse the service by issuing orders they don't
	 * pay or need.
	 */
	private String deviceId;
	
	/**
	 * Business this checkIn belongs to.
	 */
	@NotNull
	private Key<Business> business;
	
	/**
	 * Spot in this business.
	 */
	@NotNull
	private Key<Spot> spot;
	
	private boolean archived = false;
	
	private String channelId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		this.business = business;
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
		return getKey(super.getId());
	}
	
	@Transient
	public static Key<CheckIn> getKey(Long id) {
		return new Key<CheckIn>(CheckIn.class, id);
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

	public Date getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	
	
}
