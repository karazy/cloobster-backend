package net.eatsense.restws;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.counter.CounterService;
import net.eatsense.counter.Counter.PeriodType;

@Path("tasks/counter")
public class CounterTasksResource {
	private final CounterService counterService;
	private final CounterRepository counterRepo;

	@Inject
	public CounterTasksResource(CounterService counterService, CounterRepository counterRepo) {
		super();
		this.counterService = counterService;
		this.counterRepo = counterRepo;
	}
	
	@POST
	@Path("worker")
	public void processWriteback(@FormParam("name") String name,
			@FormParam("locationId") long locationId,
			@FormParam("areaId") long areaId,
			@FormParam("period") long periodTimestamp,
			@FormParam("periodType") PeriodType periodType) {
		counterService.persistCounter(name, periodType, new Date(periodTimestamp), locationId, areaId);
	}
	
	@POST
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String testCounter() {
		counterService.loadAndIncrementCounter("test", PeriodType.HOUR, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.DAY, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.MINUTE, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.SECOND, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.YEAR, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.WEEK, new Date(), 0, 0, 1);
		counterService.loadAndIncrementCounter("test", PeriodType.MONTH, new Date(), 0, 0, 1);
		return counterService.loadAndIncrementCounter("test", PeriodType.ALL, null, 0, 0, 1).toString();
	}
	
	@GET
	@Path("test")
	@Produces(MediaType.APPLICATION_JSON)
	public Iterable<Counter> getTestCounters() {
		return counterRepo.getDailyCountsByNameAreaAndLocation("test", 0, 0); 
	}
}
