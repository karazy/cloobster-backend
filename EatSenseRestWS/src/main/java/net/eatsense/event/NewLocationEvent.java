package net.eatsense.event;

import net.eatsense.domain.Location;

public class NewLocationEvent {
	private final Location location;

	public NewLocationEvent(Location location) {
		super();
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
}
