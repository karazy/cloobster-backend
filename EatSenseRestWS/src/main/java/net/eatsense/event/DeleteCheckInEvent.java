package net.eatsense.event;

import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;

public class DeleteCheckInEvent extends CheckInEvent {

	protected final Location business;
	private boolean checkOut;

	public DeleteCheckInEvent(CheckIn checkIn, Location business, boolean checkOut) {
		super(checkIn);
		this.business= business;
		this.checkOut = checkOut;
	}
	
	public Location getBusiness() {
		return business;
	}

	public boolean isCheckOut() {
		return checkOut;
	}

}
