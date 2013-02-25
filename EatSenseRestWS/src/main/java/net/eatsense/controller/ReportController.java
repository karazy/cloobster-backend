package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.representation.CounterReportDTO;

public class ReportController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CounterRepository counterRepo;
	private final AreaRepository areaRepo;
	private final LocationRepository locationRepo;

	@Inject
	public ReportController(CounterRepository counterRepo, LocationRepository locationRepo, AreaRepository areaRepo) {
		super();
		this.counterRepo = counterRepo;
		this.locationRepo = locationRepo;
		this.areaRepo = areaRepo;
	}
	
	public List<CounterReportDTO> getReportForLocationAreaAndDateRange(Business location, String kpi,  long areaId, Date fromDate, Date toDate) {
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
}
