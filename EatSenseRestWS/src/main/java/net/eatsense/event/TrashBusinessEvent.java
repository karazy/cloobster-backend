package net.eatsense.event;

import net.eatsense.domain.Business;

public class TrashBusinessEvent {
	private final Business business;

	public TrashBusinessEvent(Business business) {
		super();
		this.business = business;
	}

	public Business getBusiness() {
		return business;
	}
}
