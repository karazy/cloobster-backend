package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;

public class CustomerRequestEvent {
	private final Business business;
	private final Request request;
	
	public Business getBusiness() {
		return business;
	}

	public Request getRequest() {
		return request;
	}

	public CustomerRequestEvent(Business business, Request request) {
		super();
		this.business = business;
		this.request = request;
	}
}
