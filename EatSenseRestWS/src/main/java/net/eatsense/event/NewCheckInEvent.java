package net.eatsense.event;


import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class NewCheckInEvent extends CheckInEvent {
	public NewCheckInEvent(Business business, CheckIn checkIn) {
		super(checkIn, business);
	}
}
