package net.eatsense.counter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.OfyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
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

	public String getCounterKeyName(String name, long locationId, long areaId, PeriodType periodType, Date period) {
		// Format like this locationId:areaId:periodType:scopedPeriod:name ( e.g. "1001:101:DAY:2012-02-12:checkins")
		return String.format("%d:%d:%s:%s:%s", locationId, areaId, periodType, periodType.getScope(period), name);
	}
	
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
	
	public void persistCounter(String name, PeriodType periodType, Date period, long locationId, long areaId) {
		String keyName = getCounterKeyName(name, locationId, areaId, periodType, period);
		
		Long cachedCount = (Long) memcache.get(keyName);
		if(cachedCount == null) {
			logger.error("Unable to retrieve cached counter value for {}", keyName);
			throw new ServiceException();
		}
		
		Counter counter = ofy.find(Counter.class, keyName);
		
		if(counter == null) {
			counter = new Counter();
			counter.setAreaId(areaId);
			counter.setId(keyName);
			counter.setLocationId(locationId);
			counter.setName(name);
			counter.setPeriod(period);
			counter.setPeriodType(periodType);
		}
		
		counter.setCount(cachedCount);
		logger.info("Saving Counter: id={}, value={}", keyName, cachedCount);
		ofy.put(counter);
		
		memcache.delete(keyName + "_dirty");
	}
}
