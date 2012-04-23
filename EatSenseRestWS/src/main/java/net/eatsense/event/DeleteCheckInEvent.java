package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class DeleteCheckInEvent extends CheckInEvent {

	private boolean checkOut;

	public DeleteCheckInEvent(CheckIn checkIn, Business business, boolean checkOut) {
		super(checkIn, business);
		this.checkOut = checkOut;
	}

	public boolean isCheckOut() {
		return checkOut;
	}

}
