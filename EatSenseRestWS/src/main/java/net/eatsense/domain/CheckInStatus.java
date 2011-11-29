package net.eatsense.domain;

/**
 * Status codes for a user/guests restaurant visit. 
 * 
 * @author Frederik Reifschneider
 *
 */
public enum CheckInStatus {
	
	/**
	 * User tries to checkin
	 */
	INTENT, 
	/**
	 * User checked in.
	 */
	CHECKEDIN, 
	/**
	 * User placed an order.
	 */
	ORDER_PLACED,
	/**
	 * User is in an idle state. At least on order was served.
	 */
	ORDER_PROCESSED,
	/**
	 * User wants to pay.
	 */
	PAYMENT_REQUEST

}
