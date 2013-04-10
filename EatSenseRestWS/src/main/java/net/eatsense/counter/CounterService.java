package net.eatsense.counter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.OfyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.googlecode.objectify.Objectify;

/**
 * Handles counter creation and counting as well as persisting counters in the datastore.
 * 
 * @author Nils Weiher
 *
 */
public class CounterService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String COUNTER_WRITEBACK_PATH = "/tasks/counter/worker";
	private final MemcacheService memcache;
	private final OfyService ofyService;
	private final Queue taskQueue;

	private final Objectify ofy;
	
	@Inject
	public CounterService(MemcacheService memcache, OfyService ofyService,@Named("counter-writebacks") Queue taskQueue) {
		super();
		this.memcache = memcache;
		this.ofyService = ofyService;
		this.taskQueue = taskQueue;
		this.ofy = ofyService.ofy();
	}

	/**
	 * Build the id of a counter from the arguments.
	 * 
	 * @param name
	 * @param locationId
	 * @param areaId
	 * @param periodType
	 * @param period
	 * @return {@link String} identifier unique for this counter.
	 */
	public String getCounterKeyName(String name, long locationId, long areaId, PeriodType periodType, Date period) {
		// Format like this locationId:areaId:periodType:scopedPeriod:name ( e.g. "1001:101:DAY:2012-02-12:checkins")
		return String.format("%d:%d:%s:%s:%s", locationId, areaId, periodType, periodType.getScope(period), name);
	}
	
	/**
	 * @param counterKey
	 * @return
	 */
	public String getCounterNameFromKey(String counterKey) {
		return counterKey.substring(counterKey.lastIndexOf(":")+1);
	}
	
	/**
	 * @param counterKey
	 * @return a format pattern used to replace area id in an existing counter key
	 */
	public String getCounterKeyFormatForArea(String counterKey) {
		String[] parts = counterKey.split(":");
		return String.format("%s:%%d:%s:%s:%s", parts[0], parts[2], parts[3],parts[4]);
	}
	
	/**
	 * @param name
	 * @param locationId
	 * @param areaId
	 * @param periodType
	 * @param period
	 * @return a format pattern used to replace area id in an existing counter key
	 */
	public String getCounterKeyFormatWithAreaPlaceholder(String name, long locationId, PeriodType periodType, Date period) {
		return String.format("%d:%%d:%s:%s:%s", locationId, periodType, periodType.getScope(period), name);
	}
	
	/**
	 * Load a counter from memcache or the datastore if not existing in cache.
	 * 
	 * @param name
	 * @param periodType
	 * @param period
	 * @param locationId
	 * @param areaId
	 * @return Value of the counter or <code>null</code> if not existing.
	 */
	public Long loadAndGetCounter(String name, PeriodType periodType, Date period, long locationId, long areaId) {
		String keyName = getCounterKeyName(name, locationId, areaId, periodType, period);
		
		Long count = (Long) memcache.get(keyName);
		
		if(count == null) {
			Counter counter = ofy.find(Counter.class, keyName);
			if(counter != null) {
				count = counter.getCount();
				memcache.put(keyName, count);
			}
		}
		
		return count;
	}
	
	/**
	 * @param counterKeys
	 * @return {@link Map} of all counter values if found
	 */
	public Map<String, Object> loadAndGetCounters(Collection<String> counterKeys) {
		
		Map<String, Object> allCounters = memcache.getAll(counterKeys);
		Set<String> countersToLoad = Sets.newHashSet(counterKeys);
		countersToLoad.removeAll(allCounters.keySet());
		
		for (Counter counter : ofy.get(Counter.class, countersToLoad).values()) {
			allCounters.put(counter.getId(), counter.getCount());
		} 
		
		return allCounters;
	}
	
	/**
	 * Increment a counter identified by the supplied arguments.
	 * Creates a new counter if none exists under the given name.
	 * 
	 * @param name
	 * @param periodType
	 * @param period
	 * @param locationId
	 * @param areaId
	 * @param delta
	 * @return
	 */
	public Long loadAndIncrementCounter(String name, PeriodType periodType, Date period, long locationId, long areaId, int delta) {
		checkNotNull(name, "name was null");
		
		String keyName = getCounterKeyName(name, locationId, areaId, periodType, period);
		
		Long result = memcache.increment(keyName, delta);
		
		if(result == null) {
			Counter counter = ofy.find(Counter.class, keyName);
			long newCounterValue = delta;
			if(counter != null) {
				newCounterValue = counter.getCount() + delta;
				// Do not go below 0!
				if(newCounterValue < 0)
					newCounterValue = 0;
			}
			result = new Long(newCounterValue);
			memcache.put(keyName, newCounterValue);
		}
		
		// Set dirty flag for this counter and dispatch write task
		if(memcache.put(keyName + "_dirty", delta,null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT)) {
			long time = period != null ? period.getTime() : 0;
			taskQueue.add(TaskOptions.Builder.withUrl(COUNTER_WRITEBACK_PATH)
					.param("name", name)
					.param("period", String.valueOf(time))
					.param("periodType", periodType.toString())
					.param("locationId", String.valueOf(locationId))
					.param("areaId", String.valueOf(areaId)));
		}
		
		return result;
	}
	
	/**
	 * Convenience method to save counter with no override value.
	 * {@link #persistCounter(String, PeriodType, Date, long, long, Optional)}
	 * 
	 * @param name
	 * @param periodType
	 * @param period
	 * @param locationId
	 * @param areaId
	 */
	public void persistCounter(String name, PeriodType periodType, Date period, long locationId, long areaId) {
		persistCounter(name, periodType, period, locationId, areaId, Optional.<Long>absent());
	}
	
	/**
	 * Worker method to save a counter to the datastore.
	 * Usually called by a task queue resource asynchronously.
	 * 
	 * @param name
	 * @param periodType
	 * @param period
	 * @param locationId
	 * @param areaId
	 * @Param overrideValue optional value, if specified dont load counter from cache
	 */
	public void persistCounter(String name, PeriodType periodType, Date period, long locationId, long areaId,Optional<Long> overrideValue) {
		String keyName = getCounterKeyName(name, locationId, areaId, periodType, period);
		
		Long count = null;
		
		if(!overrideValue.isPresent()) {
			count = (Long) memcache.get(keyName);
			if(count == null) {
				logger.error("Unable to retrieve cached counter value for {}", keyName);
				throw new ServiceException();
			}			
		}
		else {
			count = overrideValue.get();
		}
		
		
		Counter counter = new Counter();
		counter.setAreaId(areaId);
		counter.setId(keyName);
		counter.setLocationId(locationId);
		counter.setName(name);
		counter.setPeriod(period);
		counter.setPeriodType(periodType);
		counter.setCount(count);
		
		saveCounter(counter, overrideValue.isPresent());
		
		memcache.delete(keyName + "_dirty");
	}
	
	/**
	 * @param counter
	 * @param loadToCache
	 */
	public void saveCounter(Counter counter, boolean loadToCache) {
		logger.info("Saving Counter: id={}, value={}", counter.getId(), counter.getCount());
		ofy.put(counter);
		if(loadToCache)
			memcache.put(counter.getId(), counter.getCount());
	}
}
