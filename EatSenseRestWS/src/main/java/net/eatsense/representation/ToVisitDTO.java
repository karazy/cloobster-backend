package net.eatsense.representation;

import java.util.Date;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.Spot;
import net.eatsense.domain.Visit;

import com.google.appengine.api.datastore.GeoPt;
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
	
	private GeoPt geoLocation;
	
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
		this.geoLocation = visit.getGeoLocation();
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

	public GeoPt getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoPt geoLocation) {
		this.geoLocation = geoLocation;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public final static Function<Visit, ToVisitDTO> toDTO = 
			new Function<Visit, ToVisitDTO>() {
				@Override
				public ToVisitDTO apply(Visit input) {
					return new ToVisitDTO(input);
				}
		    };
}
