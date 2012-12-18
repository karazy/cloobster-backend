package net.eatsense.event;

import net.eatsense.domain.Location;
import net.eatsense.domain.Request;

public class CustomerRequestEvent {
	private final Location business;
	private final Request request;
	
	public Location getBusiness() {
		return business;
	}

	public Request getRequest() {
		return request;
	}

	public CustomerRequestEvent(Location business, Request request) {
		super();
		this.business = business;
		this.request = request;
	}
}
