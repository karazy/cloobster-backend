package net.eatsense.counter;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.OfyService;

public class CounterRepository {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final OfyService ofyService;
	private final Objectify ofy;
	private CounterService counterService;
	
	@Inject
	public CounterRepository(OfyService ofyService, CounterService counterService) {
		super();
		this.ofyService = ofyService;
		this.ofy = ofyService.ofy();
		this.counterService = counterService;
	}
	
	public Iterable<Counter> getDailyCountsByNameAreaAndLocation(String name, long locationId, long areaId) {
		Query<Counter> query = ofy.query(Counter.class).filter("periodType", PeriodType.DAY);
		
		if(!Strings.isNullOrEmpty(name)) {
			query = query.filter("name", name);
		}
		if(locationId != 0) {
			query = query.filter("locationId", locationId);
		}
		if(areaId != 0) {
			query = query.filter("areaId", areaId);
		}
		
		return query.fetch();		
	}
	
	public Collection<Counter> getByKeys(List<String> ids) {
		return ofy.get(Counter.class, ids).values();
	}
	
	public Collection<Counter> getDailyCountsByNameAreaLocationAndDateRange(String name, long locationId, long areaId, Date fromDate, Date toDate) {
		checkNotNull(name, "name was null");
		checkNotNull(fromDate, "fromDate was null");
		
		List<Key<Counter>> counterKeys = new ArrayList<Key<Counter>>();
		
		Date now = new Date();
		if(toDate == null) {
			toDate = now;
		}
		
		if(fromDate.after(toDate)) {
			logger.warn("fromDate can't be after toDate");
			throw new ValidationException("fromDate can't be after toDate");
		}
		
		if(toDate.after(now))
			toDate = now;
		
		// Create keys
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fromDate);
		
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, PeriodType.DAY, fromDate);
		// Add first key
		//logger.info("name={}", name);
		//logger.info("fromDate={}, toDate={}", fromDate, toDate);
		counterKeys.add(ofyService.keys().create(Counter.class, keyName));
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		while(calendar.getTime().compareTo(toDate) <= 0) {
			keyName = counterService.getCounterKeyName(name, locationId, areaId, PeriodType.DAY, calendar.getTime());		
			//logger.info("Loading counter={}", keyName);
			counterKeys.add(ofyService.keys().create(Counter.class, keyName));
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return ofy.get(counterKeys).values();
	}
}
