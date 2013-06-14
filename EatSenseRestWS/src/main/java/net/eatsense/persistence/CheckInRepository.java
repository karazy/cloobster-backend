package net.eatsense.persistence;

import java.util.List;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

public class CheckInRepository extends GenericRepository<CheckIn> {
	final static Class<CheckIn> entityClass = CheckIn.class;
	
	private final MemcacheService memcache;
	
	@Inject
	public CheckInRepository(MemcacheService memcache) {
		super(CheckIn.class);
		this.memcache = memcache;
	}
	
	private String putCachedKeyByUserId(String userId, Key<CheckIn> key) {
		String cacheKey = "checkIn_"+ userId;
		// Put the raw datastore key only, we dont need the typed key
		
		logger.debug("Putting to cache (key={})", cacheKey);
		memcache.put(cacheKey, key.getRaw(),null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		
		return cacheKey;
	}
	
	private Key<CheckIn> getCachedKeyByUserId(String userId) {
		String key = "checkIn_"+ userId; 
		logger.debug("cacheKey={}", key);
		// Get the raw key saved under this identifier
		com.google.appengine.api.datastore.Key checkInKey =  (com.google.appengine.api.datastore.Key) memcache.get(key);
		
		if(checkInKey != null) {
			return Key.typed(checkInKey);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Uses memcache to translate userId to a datastore key for the corresponding checkIn. 
	 * 
	 * @param userId
	 * @return <code>null</code> if not found, or {@link CheckIn} stored under this userId
	 */
	public CheckIn getByUserId(String userId) {
		Key<CheckIn> checkInKey = getCachedKeyByUserId(userId);
		if(checkInKey != null) {
			logger.debug("Cached key found {}",checkInKey);
			return ofy().find(checkInKey);
		}
		else {
			logger.debug("No cached key found. Querying datastore ...");
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
		putCachedKeyByUserId(obj.getUserId(), key);
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
