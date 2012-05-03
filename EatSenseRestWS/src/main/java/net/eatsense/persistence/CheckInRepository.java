package net.eatsense.persistence;

import java.util.List;

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
	
	public List<CheckIn> getBySpot(Key<Spot> spotKey) {
		return ofy().query(clazz).filter("spot", spotKey).filter("archived", false).list();
	}
}
