package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.Subscription;

public class NewPendingSubscription {
	private final Subscription newSubscription;
	private final Business location;
	
	public NewPendingSubscription(Subscription newSubscription,
			Business location) {
		super();
		this.newSubscription = newSubscription;
		this.location = location;
	}

	public Subscription getNewSubscription() {
		return newSubscription;
	}

	public Business getLocation() {
		return location;
	}
}
