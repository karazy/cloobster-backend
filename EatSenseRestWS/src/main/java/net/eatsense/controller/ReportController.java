package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.counter.CounterService;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.representation.CounterReportDTO;
import net.eatsense.representation.LocationReportDTO;

public class ReportController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CounterRepository counterRepo;
	private final AreaRepository areaRepo;
	private final LocationRepository locationRepo;
	private final CompanyRepository companyRepo;
	private final CounterService counterService;
	
	private final static ImmutableSet<String> counterNamesForReporting = ImmutableSet.of("checkins", "orders-placed", "customer-requests","feedback");

	@Inject
	public ReportController(CounterRepository counterRepo, LocationRepository locationRepo, AreaRepository areaRepo, CompanyRepository companyRepo, CounterService counterService) {
		super();
		this.counterRepo = counterRepo;
		this.locationRepo = locationRepo;
		this.areaRepo = areaRepo;
		this.companyRepo = companyRepo;
		this.counterService = counterService;
	}
	
	public List<CounterReportDTO> getReportForLocationAreaAndDateRange(Business location, String kpi,  long areaId, Date fromDate, Date toDate) {
		checkNotNull(location, "location was null");
		checkArgument(!Strings.isNullOrEmpty(kpi), "kpi was null or empty");
		
		if(Strings.isNullOrEmpty(kpi)) {
			logger.warn("kpi was not set or empty");
			throw new ValidationException("kpi was not set or empty");
		}
			
		if(fromDate == null) {
			logger.warn("fromDate was not set");
			throw new ValidationException("fromDate was not set");
		}
		
		
		List<CounterReportDTO> counterReports = new ArrayList<CounterReportDTO>();
		
		if(areaId != 0) {
			Area area;
			try {
				area = areaRepo.getById(location.getKey(), areaId);
			} catch (NotFoundException e) {
				logger.warn("areaId unknown: {}", areaId);
				throw new ValidationException("areaId unknown");
			}
			
			Collection<Counter> counters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange(kpi, location.getId(), areaId, fromDate, toDate);
			
			for (Counter counter : counters) {
				counterReports.add(new CounterReportDTO(counter, location.getName(), area.getName()));
			}
		}
		else {
			Iterable<Area> areas = areaRepo.iterateByParent(location.getKey());
			
			for (Area area2 : areas) {
				Collection<Counter> areaCounters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange(kpi, location.getId(), area2.getId(), fromDate, toDate);
				
				for (Counter counter : areaCounters) {
					counterReports.add(new CounterReportDTO(counter, location.getName(), area2.getName()));
				}
			}
		}
		
		return counterReports;
	}
	
	public List<LocationReportDTO> getReportForAllLocationsAndKPIs( Date fromDate, Date toDate) {
		if(fromDate == null) {
			logger.warn("fromDate was not set");
			throw new ValidationException("fromDate was not set");
		}
		if(toDate == null) {
			toDate = new Date();
		}
		// Get all not deleted locations
		Iterable<Business> allLocations = locationRepo.iterateByProperty("trash", false);
		// Company map for duplicate requests
		Map<Key<Company>, Company> companyMap = Maps.newHashMap();
		List<LocationReportDTO> allLocationsReport = Lists.newArrayList(); 
		
		for (Business location : allLocations) {
			LocationReportDTO report = new LocationReportDTO();
			report.setId(String.format("%d:ALL", location.getId()));
			report.setLocationId(location.getId());
			report.setLocationName(location.getName());
			if(location.getCompany() != null) {
				report.setCompanyId(location.getCompany().getId());
				Company company = companyMap.get(location.getCompany());
				if(company == null ){
					company = companyRepo.getByKey(location.getCompany());
					companyMap.put(location.getCompany(), company);
				}
				report.setCompanyName(company.getName());				
			}
		
			Collection<Counter> checkinCounters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange("checkins", location.getId(), 0, fromDate, toDate);
			for (Counter counter : checkinCounters) {
				report.setCheckInCount(report.getCheckInCount() + counter.getCount());
			}
			Collection<Counter> orderCounters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange("orders-placed", location.getId(), 0, fromDate, toDate);
			for (Counter counter : orderCounters) {
				report.setOrderCount(report.getOrderCount() + counter.getCount());
			}
			
			Collection<Counter> serviceCallCounters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange("customer-requests", location.getId(), 0, fromDate, toDate);
			for (Counter counter : serviceCallCounters) {
				report.setServiceCallCount(report.getServiceCallCount() + counter.getCount());
			}
			
			Collection<Counter> feedbackCounters = counterRepo.getDailyCountsByNameAreaLocationAndDateRange("feedback", location.getId(), 0, fromDate, toDate);
			for (Counter counter : feedbackCounters) {
				report.setFeedbackCount(report.getFeedbackCount() + counter.getCount());
			}
			
			allLocationsReport.add(report);
		}
		
		return allLocationsReport;
	}
	
	/**
	 * Sum up all daily counters for all locations and save total over all areas for each location.
	 * Should be called from a back
	 */
	public void generateDailyLocationCounterReport(Optional<Date> optionalDate) {
		// Get all not deleted locations
		Iterable<Business> allLocations = locationRepo.iterateByProperty("trash", false);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		
		Date dateToCount = optionalDate.or(calendar.getTime());
		
		Map<String, Long> totalCounts = Maps.newHashMap();
		
		for (Business location : allLocations) {
			List<Key<Area>> areas = areaRepo.getKeysByParent(location.getKey());
			for (String counterName : counterNamesForReporting) {
				String counterKeyFormat = counterService.getCounterKeyFormatWithAreaPlaceholder(counterName, location.getId(), PeriodType.DAY, dateToCount);
				
				ArrayList<String> areaCounterKeys = Lists.newArrayList();
				for (Key<Area> key : areas) {
					areaCounterKeys.add(String.format(counterKeyFormat, key.getId()));
				}
				long counterValue = 0;
				for (Object value : counterService.loadAndGetCounters(areaCounterKeys).values()) {					
					Long areaValue = (Long) value;
					counterValue += areaValue;
				}
				
				// Save the sum for this location and kpi.
				counterService.persistCounter(counterName, PeriodType.DAY, dateToCount, location.getId(), 0, Optional.of(counterValue));
				
				// Add to total count over all locations.
				Long totalCount = totalCounts.get(counterName);
				if(totalCount == null) {
					totalCounts.put(counterName, counterValue);
				}				
				else {
					totalCounts.put(counterName, counterValue + totalCount);
				}
			}
		}
		
		// Persist overall counter values.
		for (Entry<String, Long> totalCountEntry : totalCounts.entrySet()) {
			counterService.persistCounter(totalCountEntry.getKey(), PeriodType.DAY, dateToCount, 0, 0, Optional.of(totalCountEntry.getValue()));
		}
	}
}
