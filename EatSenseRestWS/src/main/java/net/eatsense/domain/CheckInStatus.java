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
	 * Returned after check in and indicating that others are checked in at the same spot.
	 */
	YOUARENOTALONE,
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
