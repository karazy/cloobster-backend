package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Request extends GenericEntity {
	public enum RequestType {
		ORDER,
		BILL
	}
	RequestType type;
	String status;
	Long objectId;
	Key<Spot> spot;
	Key<CheckIn> checkIn;
	
	@Parent
	Key<Restaurant> restaurant;
	Date receivedTime;
	
	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}
	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}
	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
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
		
		return getKey(super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<Request> getKey(Long id) {
		
		return new Key<Request>(Request.class, id);
	}
}