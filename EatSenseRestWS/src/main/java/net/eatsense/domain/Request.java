package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class Request extends GenericEntity<Request> {
	public enum RequestType {
		ORDER,
		BILL,
		CUSTOM
	}
	RequestType type;
	String status;
	// Represents the datastore id of the corresponding of this request (type == RequestType.ORDER means objectId is id of the order object)
	Long objectId;
	Key<Spot> spot;
	Key<CheckIn> checkIn;
	@Unindexed
	
	private String spotName;
	@Unindexed
	private String checkInName;
	
	@Parent
	Key<Business> business;
	Date receivedTime;
	
	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}
	public Key<Business> getBusiness() {
		return business;
	}
	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
	public RequestType getType() {
		return type;
	}
	public void setType(RequestType type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public Key<Spot> getSpot() {
		return spot;
	}
	public void setSpot(Key<Spot> spot) {
		this.spot = spot;
	}
	public Date getReceivedTime() {
		return receivedTime;
	}
	public void setReceivedTime(Date receivedTime) {
		this.receivedTime = receivedTime;
	}
	
	@Transient
	@JsonIgnore
	public Key<Request> getKey() {
		
		return getKey(getBusiness(), super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<Request> getKey(Key<Business> parent, Long id) {
		
		return new Key<Request>(parent, Request.class, id);
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
}
