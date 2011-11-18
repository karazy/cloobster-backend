package net.eatsense.representation;



/**
 * Represents information transferred after submitting a barcode for checkIn.
 * This class is intended only for representation and will NOT BE persisted.
 * 
 * @author Frederik Reifschneider
 * 
 */
//@XmlRootElement
public class CheckIn {

	/**
	 * Status indicating for example if a restaurant with this code was found.
	 */
	private String status;

	/**
	 * Restaurants name
	 */
	private String restaurantName;

	/**
	 * Name of location inside the restaurant
	 */
	private String locationName;

	/**
	 * The sessionId used to identify this user.
	 */
	private String sessionId;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
