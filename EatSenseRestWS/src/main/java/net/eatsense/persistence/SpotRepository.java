package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;

public class SpotRepository extends GenericRepository<Spot> {
	public SpotRepository() {
		super(Spot.class);
	}
	
	public Iterable<Spot> belongingToLocationAsync(Business location) {
		logger.info("for Business({})", location.getId());
		return query().ancestor(location).fetch();
	}
	
	/**
	 * @param locationKey
	 * @return "welcome"-Spot for the location or <code>null</code>, if not found.
	 */
	public Spot belongingToLocationAndWelcomeSpot(Key<Business> locationKey) {
		logger.info("location=", locationKey);
		return query().ancestor(locationKey).filter("welcome", true).get();
	}
}
