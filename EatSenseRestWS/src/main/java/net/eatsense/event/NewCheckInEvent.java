package net.eatsense.event;


import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class NewCheckInEvent extends CheckInEvent {
	protected final Business business;

	public NewCheckInEvent(CheckIn checkIn, Business business) {
		super(checkIn);
		this.business = business;
	}

	public Business getBusiness() {
		return business;
	}

}
