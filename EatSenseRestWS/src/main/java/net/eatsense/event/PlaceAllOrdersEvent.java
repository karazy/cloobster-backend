package net.eatsense.event;

import net.eatsense.domain.CheckIn;

public class PlaceAllOrdersEvent extends MultiUpdateEvent {

	public PlaceAllOrdersEvent(CheckIn checkIn, int entityCount) {
		super(checkIn, entityCount);
	}

}
