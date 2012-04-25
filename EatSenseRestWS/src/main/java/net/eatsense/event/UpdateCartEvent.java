package net.eatsense.event;

import net.eatsense.domain.CheckIn;

public class UpdateCartEvent extends CheckInEvent {

	public UpdateCartEvent(CheckIn checkIn) {
		super(checkIn);
	}

}
