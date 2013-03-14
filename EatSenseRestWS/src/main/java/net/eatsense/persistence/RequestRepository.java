package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;

public class RequestRepository extends GenericRepository<Request> {
	public RequestRepository() {
		super(Request.class);
	}
	
	public Iterable<Request> belongingToSpotOrderedByReceivedTime(Key<Spot> spotKey) {
		logger.info("for {}", spotKey);
		return query().filter("spot", spotKey).order("-receivedTime").fetch();
	}
	
	/**
	 * @param spotKey
	 * @return id field of oldest Request entity or <code>null</code> if none found.
	 */
	public Long getIdOfOldestRequestBelongingToSpot(Key<Spot> spotKey) {
		logger.info("for {}", spotKey);
		Key<Request> key = query().filter("spot", spotKey).order("-receivedTime").getKey();
		return (key == null) ? null : key.getId();
	}
	
	public Iterable<Request> belongingToLocationOrderedByReceivedTime(Business location) {
		logger.info("for Business({})", location.getId());
		return query().ancestor(location).order("-receivedTime").fetch();
	}
}
