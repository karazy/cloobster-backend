package net.eatsense.event;

import net.eatsense.domain.Location;

public class TrashBusinessEvent {
	private final Location business;

	public TrashBusinessEvent(Location business) {
		super();
		this.business = business;
	}

	public Location getBusiness() {
		return business;
	}
}
