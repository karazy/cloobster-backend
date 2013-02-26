package net.eatsense.counter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.persistence.OfyService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Objectify;

@RunWith(MockitoJUnitRunner.class)
public class CounterServiceTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Mock
	private MemcacheService memcache;
	@Mock
	private OfyService ofyService;
	@Mock
	private Queue taskQueue;

	private CounterService counterService;

	@Mock
	private Objectify ofy;

	@Before
	public void setUp() throws Exception {
		when(ofyService.ofy()).thenReturn(ofy);
		
		counterService = new CounterService(memcache, ofyService, taskQueue);
	}

	@Test
	public void testGetCounterKeyName() {
		Date now = new Date();
		String counterKeyName = counterService.getCounterKeyName("test", 123l, 11l, PeriodType.DAY, now);
		assertThat(counterKeyName, is(String.format("%d:%d:%s:%s:%s", 123l,11l, PeriodType.DAY, PeriodType.DAY.getScope(now), "test")));
	}

	@Test
	public void testLoadAndGetCounterFromCache() {
		String name = "test";
		long locationId = 123;
		long areaId = 456;
		PeriodType periodType = PeriodType.DAY;
		Date period = new Date();
		
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, periodType, period );
		Long value = new Long(1l);
		when(memcache.get(keyName)).thenReturn(value );
		
		Long returnValue = counterService.loadAndGetCounter(name, periodType, period, locationId, areaId);
		
		assertThat(returnValue, is(value));
	}
	
	@Test
	public void testLoadAndGetCounterNotCached() {
		String name = "test";
		long locationId = 123;
		long areaId = 456;
		PeriodType periodType = PeriodType.DAY;
		Date period = new Date();
		
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, periodType, period );
		Long value = new Long(1l);
		when(memcache.get(keyName)).thenReturn(null );
		
		Counter counter = new Counter();
		counter.setCount(value);
		
		when(ofy.find(Counter.class, keyName)).thenReturn(counter);
		
		Long returnValue = counterService.loadAndGetCounter(name, periodType, period, locationId, areaId);
		
		verify(memcache).put(keyName, returnValue);
		assertThat(returnValue, is(value));
	}

	@Test
	public void testLoadAndIncrementCounterInCache() {
		String name = "test";
		long locationId = 123;
		long areaId = 456;
		PeriodType periodType = PeriodType.DAY;
		Date period = new Date();
		int delta = 1;
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, periodType, period );
		Long value = 2l;

		when(memcache.increment(keyName, delta)).thenReturn(value );
		when(memcache.put(keyName+ "_dirty", delta,null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT)).thenReturn(true);
		
		counterService.loadAndIncrementCounter(name, periodType, period, locationId, areaId, delta);
		
		// Verify addition of counter writeback
		verify(taskQueue).add(any(TaskOptions.class));
	}
	
	@Test
	public void testLoadAndIncrementCounterNotInCache() {
		String name = "test";
		long locationId = 123;
		long areaId = 456;
		PeriodType periodType = PeriodType.DAY;
		Date period = new Date();
		int delta = 1;
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, periodType, period );
		Long value = 2l;

		when(memcache.increment(keyName, delta)).thenReturn(null );
		Counter counter = new Counter();
		counter.setCount(value);
		
		when(ofy.find(Counter.class, keyName)).thenReturn(counter);
		when(memcache.put(keyName+ "_dirty", delta,null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT)).thenReturn(true);
		
		counterService.loadAndIncrementCounter(name, periodType, period, locationId, areaId, delta);
		
		verify(memcache).put(keyName, value + delta);
		// Verify addition of counter writeback
		verify(taskQueue).add(any(TaskOptions.class));
	}

	@Test
	public void testPersistCounterNewCounter() {
		String name = "test";
		long locationId = 123;
		long areaId = 456;
		PeriodType periodType = PeriodType.DAY;
		Date period = new Date();
		int delta = 1;
		String keyName = counterService.getCounterKeyName(name, locationId, areaId, periodType, period );
		Long value = 2l;
		
		when(memcache.get(keyName)).thenReturn(value );

		counterService.persistCounter(name, periodType, period, locationId, areaId);
		
		ArgumentCaptor<Counter> counterCaptor = ArgumentCaptor.forClass(Counter.class);
		
		verify(ofy).put(counterCaptor.capture());
		verify(memcache).delete(keyName+"_dirty");
		
		Counter counter = counterCaptor.getValue();
		
		assertThat(counter.getAreaId(), is(areaId));
		assertThat(counter.getCount(), is(value));
		assertThat(counter.getId(), is(keyName));
		assertThat(counter.getLocationId(), is(locationId));
		assertThat(counter.getName(), is(name));
		assertThat(counter.getPeriod(), is(period));
		assertThat(counter.getPeriodType(), is(periodType));
		
	}
}
