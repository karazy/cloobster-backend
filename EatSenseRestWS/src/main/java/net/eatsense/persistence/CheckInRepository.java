package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

public class CheckInRepository extends GenericRepository<CheckIn> {
	
	public CheckInRepository() {
		super();
		super.clazz = CheckIn.class;
	}

	public int countActiveCheckInsAtSpot (Key<Spot> key) {
		return ofy().query(clazz).filter("spot", key).filter("archived", false).count();
	}
}
