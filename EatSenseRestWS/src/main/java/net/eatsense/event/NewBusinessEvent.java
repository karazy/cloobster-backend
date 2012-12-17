package net.eatsense.event;

import net.eatsense.domain.Business;

public class NewBusinessEvent {
	private final Business business;

	public NewBusinessEvent(Business business) {
		super();
		this.business = business;
	}

	public Business getBusiness() {
		return business;
	}
}
