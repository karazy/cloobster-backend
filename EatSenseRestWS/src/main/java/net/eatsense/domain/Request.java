package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents an active request issued by the customer, waiting to be acknowledged.
 * 
 * @author Nils Weiher
 *
 */
@Cached
public class Request extends GenericEntity<Request> {
	
	/**
	 * 
	 * 
	 * @author Nils Weiher
	 *
	 */
	public enum RequestType {
		ORDER,
		BILL,
		CUSTOM
	}
	
	private RequestType type;
	private String status;
	/**
	 * Represents the datastore id of the corresponding of this request ( {@link Request#type}=={@link RequestType#ORDER} means objectId is id of the order object)
	 */
	private Long objectId;
	@Unindexed
	private String objectText;
	private Key<Spot> spot;
	private Key<CheckIn> checkIn;
	private Key<Area> area;
	@Unindexed
	private String spotName;
	@Unindexed
	private String checkInName;
	
	@Parent
	private Key<Business> business;
	
	private Date receivedTime;
	
	public Request() {
		super();
		this.receivedTime = new Date();
		this.type = RequestType.CUSTOM;
	}
	
	/**
	 * Create a Request linked to the given Spot and CheckIn.
	 * Default type is {@link RequestType#CUSTOM}.
	 *  
	 * @param checkIn
	 * @param spot
	 */
	public Request(CheckIn checkIn, Spot spot) {
		this();
		
		if(checkIn != null) {
			this.business = checkIn.getBusiness();
			this.checkIn = checkIn.getKey();
			this.checkInName = checkIn.getNickname();
		}
		
		if(spot != null) {
			this.spot = spot.getKey();
			this.spotName = spot.getName();
			this.area = spot.getArea();
		}
	}
	
	public Request(CheckIn checkIn, Spot spot, Order order) {
		this(checkIn, spot);
		
		this.type = RequestType.ORDER;
		this.objectId = order.getId();
	}
	
	public Request(CheckIn checkIn, Spot spot, Bill bill) {
		this(checkIn, spot);
		
		this.type = RequestType.BILL;
		this.objectId = bill.getId();
	}
	
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

	public Key<Area> getArea() {
		return area;
	}

	public void setArea(Key<Area> area) {
		this.area = area;
	}

	public String getObjectText() {
		return objectText;
	}

	public void setObjectText(String objectText) {
		this.objectText = objectText;
	}
}
