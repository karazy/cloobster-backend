package net.eatsense.event;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;

public class DeleteSpotEvent {
	private final Key<Business> location;
	private final Spot spot;
	private final int newSpotCount;
	private final boolean multiple;

	public DeleteSpotEvent(Key<Business> locationKey, Spot spot, int newSpotCount, boolean multiple) {
		super();
		this.location = locationKey;
		this.spot = spot;
		this.newSpotCount = newSpotCount;
		this.multiple = multiple;
	}

	public Key<Business> getLocation() {
		return location;
	}

	public Spot getSpot() {
		return spot;
	}

	public int getNewSpotCount() {
		return newSpotCount;
	}

	public boolean isMultiple() {
		return multiple;
	}
}
