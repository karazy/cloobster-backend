package net.eatsense.event;

import net.eatsense.domain.Business;

public class NewLocationEvent {
	private final Business location;

	public NewLocationEvent(Business location) {
		super();
		this.location = location;
	}

	public Business getLocation() {
		return location;
	}
}
