package net.eatsense.restws.administration;

import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.eatsense.controller.DashboardController;
import net.eatsense.controller.LocationController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.counter.Counter;
import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.counter.CounterRepository;
import net.eatsense.counter.CounterService;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.DashBoarditemRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.DataUpgradesResultDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class DataUpgradesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final SubscriptionController subCtrl;
	private final LocationController locationCtrl;

	private final AreaRepository areaRepo;

	private final SpotRepository spotRepo;

	private final DashboardController dashboardCtrl;

	private final LocationRepository locationRepo;

	private final DashBoarditemRepository dashboardItemRepo;

	private final UriInfo uriInfo;

	private final CounterService counterService;

	private final CounterRepository counterRepo;
	
	@Inject
	public DataUpgradesResource(SubscriptionController subCtrl,
			LocationController locationCtrl, AreaRepository areaRepo, SpotRepository spotRepo, DashboardController dashboardCtrl, LocationRepository locationRepo, DashBoarditemRepository dashboardItemRepo, UriInfo uriInfo, CounterRepository counterRepo, CounterService counterService) {
		super();
		this.subCtrl = subCtrl;
		this.locationCtrl = locationCtrl;
		this.areaRepo = areaRepo;
		this.spotRepo = spotRepo;
		this.dashboardCtrl = dashboardCtrl;
		this.locationRepo = locationRepo;
		this.dashboardItemRepo = dashboardItemRepo;
		this.uriInfo = uriInfo;
		this.counterRepo = counterRepo;
		this.counterService = counterService;
	}
	
	@PUT
	@Path("masterspots")
	@Produces("application/json")
	public DataUpgradesResultDTO updateAreasAddMasterSpot() {
		int updateCount = 0;
		
		QueryResultIterable<Area> areas = areaRepo.query().fetch();
		
		for (Area area : areas) {
			if(!area.isWelcome()) {
				boolean hasMasterSpot = false;
				Key<Area> areaKey = area.getKey();
				for (Spot spot : spotRepo.query().filter("area", areaKey)) {
					if(spot.isMaster()) {
						hasMasterSpot = true;
					}
				}
				if(!hasMasterSpot) {
					updateCount++;
					// No master Spot found, add it.
					
					locationCtrl.createMasterSpot(area.getBusiness(), areaKey);
				}				
			}
		}
		logger.info("Areas updated:  {}", updateCount);
		return new DataUpgradesResultDTO("OK", updateCount);
	}
	
	@PUT
	@Path("subscriptionandwelcome")
	@Produces("application/json")
	public DataUpgradesResultDTO updateLocationSubscriptions() {
		List<Business> allLocations = locationCtrl.getLocations(0);
		int updateCount = 0;
		for (Business business : allLocations) {
			logger.info("Checking {} for incomplete data.", business.getKey());
			// Check for a welcome spot
			if(locationCtrl.getSpots(business.getKey(), 0, true, false).isEmpty()) {
				logger.info("Creating welcome area and spot ...");
				locationCtrl.createWelcomeAreaAndSpot(business.getKey(), null);
				updateCount++;
			}
			
			if(business.getActiveSubscription() == null) {
				logger.info("Creating basic subscription ...");
				subCtrl.setBasicSubscription(business);
				updateCount++;
			}
		}
		
		return new DataUpgradesResultDTO("OK", updateCount);
	}
	
	@PUT
	@Path("defaultdashboards")
	@Produces("application/json")
	public Response queueCreateLocationDashboards() {
		QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/"+uriInfo.getPath()+"/process"));
		
		return Response.ok().build();
	}

	
	@POST
	@Path("defaultdashboards/process")
	@Produces("application/json")
	public Response createLocationDashboards() {
		for (Key<Business> locationKey : locationRepo.iterateKeysByProperty("trash", false)) {
			if(dashboardItemRepo.getConfiguration(locationKey) == null) {
				dashboardCtrl.createDefaultItems(locationKey);
			}
			else {
				logger.info("Skipped creation for {}", locationKey);
			}
			
		}
		
		return Response.ok().build();
	}
	
	@PUT
	@Path("sumdailycounters")
	@Produces("application/json")
	public Response queueGenerateDailyCounterReports() {
		QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/"+uriInfo.getPath()+"/process"));
		
		return Response.ok().build();
	}

	
	@POST
	@Path("sumdailycounters/process")
	@Produces("application/json")
	public Response generateDailyCounterReports() {
		
		Map<String, Counter> aggregatedCounters = Maps.newHashMap();
		
		for (Counter counter : counterRepo.getDailyCountsByNameAreaAndLocation(null, 0, 0)) {
			if(( counter.getLocationId() == null || counter.getLocationId().longValue() == 0) ||
					(counter.getAreaId() == null || counter.getAreaId().longValue() == 0) ) {
				logger.info("skipped: {}", counter.getId());
				// Skip if we have an already summed up counter
				continue;
			}
			
			String counterKey = counterService.getCounterKeyName(counter.getName(), counter.getLocationId(), 0, PeriodType.DAY, counter.getPeriod());
			Counter locationCounter = aggregatedCounters.get(counterKey);
			if(locationCounter == null) {
				// Create new location counter
				locationCounter = new Counter(counter);
				locationCounter.setAreaId(0l);
				locationCounter.setId(counterKey);
				
				aggregatedCounters.put(counterKey, locationCounter );
			}
			else {
				// Add to location counter
				locationCounter.setCount(locationCounter.getCount() + counter.getCount());				
			}
			
			counterKey = counterService.getCounterKeyName(counter.getName(), 0, 0, PeriodType.DAY, counter.getPeriod());
			Counter dailyCounter = aggregatedCounters.get(counterKey);
			
			if(dailyCounter == null) {
				// Create new daily counter for all location
				dailyCounter = new Counter(counter);
				dailyCounter.setAreaId(0l);
				dailyCounter.setLocationId(0l);
				dailyCounter.setId(counterKey);
				
				aggregatedCounters.put(counterKey, dailyCounter);
			}
			else {
				// Add to daily counter
				dailyCounter.setCount(dailyCounter.getCount() + counter.getCount() );
			}
			
			counterKey = counterService.getCounterKeyName(counter.getName(), 0, 0, PeriodType.ALL, counter.getPeriod());
			
			Counter overallCounter = aggregatedCounters.get(counterKey);
			if(overallCounter == null) {
				overallCounter = new Counter(counter);
				overallCounter.setPeriodType(PeriodType.ALL);
				overallCounter.setLocationId(0l);
				overallCounter.setAreaId(0l);
				overallCounter.setId(counterKey);
				
				aggregatedCounters.put(counterKey, overallCounter);				
			}
			else {
				// Add to overall counter
				overallCounter.setCount(overallCounter.getCount() + counter.getCount());
			}
		}
		
		for (Counter newAggregateCounter : aggregatedCounters.values()) {
			counterService.saveCounter(newAggregateCounter, true);
		}
		
		return Response.ok().build();
	}
}
