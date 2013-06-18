package net.eatsense.persistence;

import java.util.List;

import net.eatsense.cache.EntityKeyCache;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class CheckInRepository extends GenericRepository<CheckIn> {
	final static Class<CheckIn> entityClass = CheckIn.class;
	private final EntityKeyCache keyCache;
	
	@Inject
	public CheckInRepository(EntityKeyCache keyCache) {
		super(CheckIn.class);
		this.keyCache = keyCache;
	}
	

	/**
	 * Uses memcache to translate userId to a datastore key for the corresponding checkIn. 
	 * 
	 * @param userId
	 * @return <code>null</code> if not found, or {@link CheckIn} stored under this userId
	 */
	public CheckIn getByUserId(String userId) {
		Key<CheckIn> checkInKey = keyCache.get(userId, CheckIn.class);
		if(checkInKey != null) {
			logger.debug("from cache. key={}",checkInKey);
			return ofy().find(checkInKey);
		}
		else {
			logger.debug("userId not cached. querying datastore ...");
			CheckIn checkIn = ofy().query(CheckIn.class).filter("userId", userId).get();
			return checkIn;
		}
	}
	
	@Override
	public CheckIn getByProperty(String propName, Object propValue) {
		if(propName.equals("userId")) {
			return getByUserId((String) propValue);
		}
		else 
			return super.getByProperty(propName, propValue);
	}
	
	@Override
	public Key<CheckIn> saveOrUpdate(CheckIn obj) {
		Key<CheckIn> key = super.saveOrUpdate(obj);
		keyCache.put(obj.getUserId(), key);
		
		return key; 
	}

	public int countActiveCheckInsAtSpot (Key<Spot> key) {
		return ofy().query(clazz).filter("spot", key).filter("archived", false).count();
	}
	
	public List<CheckIn> getBySpot(Key<Spot> spotKey) {
		return ofy().query(clazz).filter("spot", spotKey).filter("archived", false).list();
	}
	public Iterable<Key<CheckIn>> iterateKeysByLocation(Key<Business> locationKey) {
		logger.info("for {}", locationKey);
		
		return ofy().query(clazz).filter("business", locationKey).filter("archived", false).fetchKeys();
	}
	
	public Iterable<CheckIn> iterateByLocation(Key<Business> locationKey) {
		logger.info("for {}", locationKey);
		
		return ofy().query(clazz).filter("business", locationKey).filter("archived", false).fetch();
	}
}
