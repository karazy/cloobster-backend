package net.eatsense.event;

import net.eatsense.domain.Business;

public class NewLocationEvent extends LocationEvent {

	public NewLocationEvent(Business location) {
		super(location);
	}

	public Business getLocation() {
		return location;
	}
}
