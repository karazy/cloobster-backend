package net.eatsense.event;

import net.eatsense.domain.CheckIn;

public class ConfirmAllOrdersEvent extends MultiUpdateEvent {

	public ConfirmAllOrdersEvent(CheckIn checkIn, int entityCount) {
		super(checkIn, entityCount);
	}

}
