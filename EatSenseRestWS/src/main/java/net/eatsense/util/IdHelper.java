package net.eatsense.util;

import java.util.UUID;

/**
 * Helper class generates random UUID numbers.
 * 
 * @author Frederik Reifschneider
 *
 */
public class IdHelper {

	/**
	 * Generate a random uuid string.
	 * @return
	 */
	public static String generateId() {
		return UUID.randomUUID().toString();
	}

}
