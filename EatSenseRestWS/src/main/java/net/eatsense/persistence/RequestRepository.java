package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;

public class RequestRepository extends GenericRepository<Request> {
	public RequestRepository() {
		super(Request.class);
	}
	
	public Iterable<Request> belongingToSpotOrderedByReceivedTime(Key<Spot> spotKey) {
		return query().filter("spot", spotKey).order("-receivedTime").fetch();
	}
	
	/**
	 * @param spotKey
	 * @return id field of oldest Request entity or <code>null</code> if none found.
	 */
	public Long getIdOfOldestRequestBelongingToSpot(Key<Spot> spotKey) {
		Key<Request> key = query().filter("spot", spotKey).order("-receivedTime").getKey();
		return (key == null) ? null : key.getId();
	}
}
