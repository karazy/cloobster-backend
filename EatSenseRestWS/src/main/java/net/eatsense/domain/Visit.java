package net.eatsense.domain;

import java.util.Date;

import com.google.appengine.api.datastore.GeoPt;
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
	
	@Unindexed
	private String comment;
	
	private Date createdOn;
	
	private Date visitDate;
	
	private GeoPt geoLocation;
			
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
		this.locationName = locationName;
	}

	public Key<Business> getLocation() {
		return location;
	}

	public void setLocation(Key<Business> location) {
		this.location = location;
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
}
