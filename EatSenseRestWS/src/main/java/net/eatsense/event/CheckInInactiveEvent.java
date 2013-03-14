package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class CheckInInactiveEvent extends CheckInEvent {

	private final Business location;

	public CheckInInactiveEvent(CheckIn checkIn, Business location) {
		super(checkIn);
		this.location = location;
	}

	public Business getLocation() {
		return location;
	}

}
