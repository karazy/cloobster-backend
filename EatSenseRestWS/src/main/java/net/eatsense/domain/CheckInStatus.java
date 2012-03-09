package net.eatsense.domain;

/**
 * Status codes reflecting the whole process of a guests visit.
 * 
 * @author Frederik Reifschneider
 *
 */
public enum CheckInStatus {
	
	/**
	 * User tries to check in.
	 */
	INTENT,
	/**
	 * User provided a wrong barcode.
	 */
	BARCODE_ERROR,
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
	PAYMENT_REQUEST,	
	/**
	 * Error during object validation. 
	 */
	VALIDATION_ERROR,
	
	/**
	 * General error during checkin
	 */
	ERROR

}
