package net.eatsense.util;

import java.util.UUID;

public class IdHelper {

	public static String generateId() {
		return UUID.randomUUID().toString();
	}

}
