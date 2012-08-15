package net.eatsense.representation;

import java.util.Date;

import net.eatsense.domain.Request;

public class RequestDTO {
	private String type;
	private Long id;
	
	private Long checkInId;
	private Long spotId;
	
	private Date receivedTime;
	
	public RequestDTO() {
		super();
	}
	
	/**
	 * @param request Entity to use as data source.
	 */
	public RequestDTO(Request request) {
		this();
		if(request == null)
			return;
		
		this.setId(request.getId());
		
		if(request.getCheckIn() != null)
			this.setCheckInId(request.getCheckIn().getId());
		
		if(request.getSpot() != null)
			this.setSpotId(request.getSpot().getId());
		
		this.setType(request.getStatus());
		this.setReceivedTime(request.getReceivedTime());
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getCheckInId() {
		return checkInId;
	}

	public void setCheckInId(Long checkInId) {
		this.checkInId = checkInId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSpotId() {
		return spotId;
	}

	public void setSpotId(Long spotId) {
		this.spotId = spotId;
	}

	public Date getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(Date receivedTime) {
		this.receivedTime = receivedTime;
	}
	
	
}
