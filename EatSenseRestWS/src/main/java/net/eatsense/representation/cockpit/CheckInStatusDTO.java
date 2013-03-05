package net.eatsense.representation.cockpit;

import java.util.Date;

import com.google.common.base.Function;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.representation.SpotDTO;

public class CheckInStatusDTO {
	

	public CheckInStatusDTO() {
	}
	
	public CheckInStatusDTO(CheckIn checkIn) {
		if(checkIn == null)
			return;
			
		this.status = checkIn.getStatus();
		this.id = checkIn.getId();
		this.nickname = checkIn.getNickname();
		this.checkInTime = checkIn.getCheckInTime();
		this.spotId = checkIn.getSpot().getId();
		this.setLastActivity(checkIn.getLastActivity());
	}
	/**
	 * Status indicating for example if customer checked in or an order was placed.
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
	
	/**
	 * Id of spot checkIn takes place.
	 */
	private Long spotId;
	
	private Date lastActivity;

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

	public Long getSpotId() {
		return spotId;
	}

	public void setSpotId(Long spotId) {
		this.spotId = spotId;
	}
	
	public Date getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}
	
	public final static Function<CheckIn, CheckInStatusDTO> toDTO = 
			new Function<CheckIn, CheckInStatusDTO>() {
				@Override
				public CheckInStatusDTO apply(CheckIn input) {
					return new CheckInStatusDTO(input);
				}
		    };			
}
