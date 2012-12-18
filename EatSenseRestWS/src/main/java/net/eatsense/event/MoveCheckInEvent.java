package net.eatsense.event;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

public class MoveCheckInEvent extends CheckInEvent {

	protected final Location business;
	private Key<Spot> oldSpot;

	public MoveCheckInEvent(CheckIn checkIn, Location business, Key<Spot> oldSpot) {
		super(checkIn);
		this.business = business;
		this.oldSpot = oldSpot;
		// TODO Auto-generated constructor stub
	}

	public Location getBusiness() {
		return business;
	}

	public Key<Spot> getOldSpot() {
		return oldSpot;
	}

}
