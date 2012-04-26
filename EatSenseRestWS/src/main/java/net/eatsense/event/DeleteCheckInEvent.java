package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class DeleteCheckInEvent extends CheckInEvent {

	protected final Business business;
	private boolean checkOut;

	public DeleteCheckInEvent(CheckIn checkIn, Business business, boolean checkOut) {
		super(checkIn);
		this.business= business;
		this.checkOut = checkOut;
	}
	
	public Business getBusiness() {
		return business;
	}

	public boolean isCheckOut() {
		return checkOut;
	}

}
