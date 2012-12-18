package net.eatsense.event;

import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Request;

public class NewCustomerRequestEvent extends CustomerRequestEvent {

	private final CheckIn checkIn;

	public NewCustomerRequestEvent(Location business, CheckIn checkIn,
			Request request) {
		super(business, request);
		this.checkIn = checkIn;
	}

	public CheckIn getCheckIn() {
		return checkIn;
	}

}
