package net.eatsense.representation.cockpit;

import java.util.Date;

import net.eatsense.domain.CheckInStatus;

public class CheckInStatusDTO {
	/**
	 * Status indicating for example if a restaurant with this code was found.
	 */
	private CheckInStatus status;
	
	private Long id;
	
	/**
	 * A users nickname used for this checkin.
	 * E. g. Peter Pan or Funny Bee ;)
	 */
	private String nickname;

	/**
	 * Time of checkin.
	 */
	private Date checkInTime;

	public CheckInStatus getStatus() {
		return status;
	}

	public void setStatus(CheckInStatus status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	
}