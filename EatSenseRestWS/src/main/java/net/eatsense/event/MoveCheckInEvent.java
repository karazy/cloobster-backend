package net.eatsense.event;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

public class MoveCheckInEvent extends CheckInEvent {

	private Key<Spot> oldSpot;

	public MoveCheckInEvent(CheckIn checkIn, Business business, Key<Spot> oldSpot) {
		super(checkIn, business);
		this.oldSpot = oldSpot;
		// TODO Auto-generated constructor stub
	}

	public Key<Spot> getOldSpot() {
		return oldSpot;
	}

}
