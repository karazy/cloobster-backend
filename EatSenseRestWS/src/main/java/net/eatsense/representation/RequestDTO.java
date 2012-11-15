package net.eatsense.representation;

import java.util.Date;

import net.eatsense.domain.Request;

public class RequestDTO {
	private String type;
	private Long id;
	
	private Long checkInId;
	private String checkInName;
	private Long spotId;
	private String spotName;
	private Long areaId;
	private Date receivedTime;
	private String info;
	
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
		this.checkInName = request.getCheckInName();
		
		if(request.getSpot() != null)
			this.setSpotId(request.getSpot().getId());
		
		this.spotName = request.getSpotName();
		
		if(request.getArea() != null)
			this.areaId = request.getArea().getId();
		
		this.setType(request.getStatus());
		this.setReceivedTime(request.getReceivedTime());
		
		this.setInfo(request.getObjectText());
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

	public String getCheckInName() {
		return checkInName;
	}

	public void setCheckInName(String checkInName) {
		this.checkInName = checkInName;
	}

	public String getSpotName() {
		return spotName;
	}

	public void setSpotName(String spotName) {
		this.spotName = spotName;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public Date getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(Date receivedTime) {
		this.receivedTime = receivedTime;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
