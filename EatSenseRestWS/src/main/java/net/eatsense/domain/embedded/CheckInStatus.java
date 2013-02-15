package net.eatsense.domain.embedded;

/**
 * Status codes reflecting the whole process of a guests visit.
 * 
 * @author Frederik Reifschneider
 *
 */
/**
 * @author Nils
 *
 */
public enum CheckInStatus {
	
	/**
	 * User tries to check in.
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
	 * User requested the bill.
	 */
	PAYMENT_REQUEST,
	COMPLETE,
	/**
	 * User switched/checked out without placing orders.
	 */
	WAS_INACTIVE
}
