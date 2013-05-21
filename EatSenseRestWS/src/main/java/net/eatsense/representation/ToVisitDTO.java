package net.eatsense.representation;

import java.util.Date;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.Visit;

import com.google.common.base.Function;

public class ToVisitDTO {
	
	private Long id;
	
	@NotNull
	private String locationName;

	private Long locationId;
	
	private String locationRefId;
	
	private String comment;
	
	private Date createdOn;
	
	private Date visitDate;
	
	private Float geoLong;
	private Float geoLat;
	
	public ToVisitDTO() {
		super();
	}
	
	public ToVisitDTO(Visit visit) {
		this();
		this.id = visit.getId();
		this.locationName = visit.getLocationName();
		this.locationId = visit.getLocation() != null ? visit.getLocation().getId() : null;
		this.locationRefId = visit.getLocationRefId();
		this.comment = visit.getComment();
		this.createdOn = visit.getCreatedOn();
		this.visitDate = visit.getVisitDate();
		
		if(visit.getGeoLocation() != null) {
			this.geoLat = visit.getGeoLocation().getLatitude();
			this.geoLong = visit.getGeoLocation().getLongitude();
		}
	}
	
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getLocationRefId() {
		return locationRefId;
	}

	public void setLocationRefId(String locationRefId) {
		this.locationRefId = locationRefId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Float getGeoLat() {
		return geoLat;
	}

	public void setGeoLat(Float geoLat) {
		this.geoLat = geoLat;
	}

	public Float getGeoLong() {
		return geoLong;
	}

	public void setGeoLong(Float geoLong) {
		this.geoLong = geoLong;
	}

	public final static Function<Visit, ToVisitDTO> toDTO = 
			new Function<Visit, ToVisitDTO>() {
				@Override
				public ToVisitDTO apply(Visit input) {
					return new ToVisitDTO(input);
				}
		    };
}
