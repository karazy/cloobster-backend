package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import net.eatsense.representation.ImageDTO;

import com.google.appengine.api.datastore.GeoPt;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a location an app user marks as "to visit".
 * It could either be a cloobster business, or just a place.
 * 
 * @author Nils Weiher
 *
 */
public class Visit extends GenericEntity<Visit> {
	
	@Parent
	private Key<Account> account;
	
	private String locationName;
	
	private Key<Business> location;
	
	private String locationRefId;
	private String locationCity;
	
	
	@Unindexed
	private String comment;
	
	private Date createdOn;
	
	private Date visitDate;
	
	private GeoPt geoLocation;
	
	private List<ImageDTO> images;
	
	@Override
	public Key<Visit> getKey() {
		return Key.create(account, Visit.class, getId());
	}

	public Key<Account> getAccount() {
		return account;
	}

	public void setAccount(Key<Account> account) {
		this.account = account;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		if(!Objects.equal(this.locationName, locationName)) {
			this.setDirty(true);
			this.locationName = locationName;
		}
	}

	public Key<Business> getLocation() {
		return location;
	}

	public void setLocation(Key<Business> location) {
		if(!Objects.equal(this.location, location)) {
			this.setDirty(true);
			this.location = location;
		}
	}

	public String getLocationRefId() {
		return locationRefId;
	}

	public void setLocationRefId(String locationRefId) {
		if(!Objects.equal(this.locationRefId, locationRefId)) {
			this.setDirty(true);
			this.locationRefId = locationRefId;
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if(!Objects.equal(this.comment, comment)) {
			this.setDirty(true);
			this.comment = comment;
		}
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		if(!Objects.equal(this.createdOn, createdOn)) {
			this.setDirty(true);
			this.createdOn = createdOn;
		}
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		if(!Objects.equal(this.visitDate, visitDate)) {
			this.setDirty(true);
			this.visitDate = visitDate;
		}
	}

	public GeoPt getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoPt geoLocation) {
		if(!Objects.equal(this.geoLocation, geoLocation)) {
			this.setDirty(true);
			this.geoLocation = geoLocation;
		}
	}

	public String getLocationCity() {
		return locationCity;
	}

	public void setLocationCity(String locationCity) {
		if(!Objects.equal(this.locationCity, locationCity)) {
			this.setDirty(true);
			this.locationCity = locationCity;
		}
	}

	public List<ImageDTO> getImages() {
		return images;
	}

	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}
}
