package net.eatsense.event;

import net.eatsense.domain.Business;

import com.googlecode.objectify.Key;

/**
 * Event send to signalize that no cockpits are connected for this location.
 * 
 * @author Nils Weiher
 *
 */
public class LocationCockpitsOfflineEvent {

	private final Key<Business> locationKey;

	public LocationCockpitsOfflineEvent(Key<Business> locationKey) {
		this.locationKey = locationKey;
	}

	public Key<Business> getLocationKey() {
		return locationKey;
	}
}
