package net.eatsense.event;


import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;

public class NewCheckInEvent extends CheckInEvent {
	protected final Location business;

	public NewCheckInEvent(CheckIn checkIn, Location business) {
		super(checkIn);
		this.business = business;
	}

	public Location getBusiness() {
		return business;
	}

}
