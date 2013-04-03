package net.eatsense.representation;

import java.util.Date;

import net.eatsense.counter.Counter;
import net.eatsense.counter.Counter.PeriodType;

public class CounterReportDTO {
	private String id;
	private String kpi;
	private double count;
	private Date date;
	private long locationId;
	private String locationName;
	private long areaId;
	private String areaName;
	private PeriodType periodType;
	
	public CounterReportDTO() {
	}
	
	public CounterReportDTO(Counter counter, String locationName, String areaName) {
		this.id = counter.getId();
		this.kpi = counter.getName();
		this.count = counter.getCount();
		this.date = counter.getPeriod();
		this.locationId = counter.getLocationId();
		this.areaId = counter.getAreaId();
		this.setPeriodType(counter.getPeriodType());
		
		this.locationName = locationName;
		this.areaName = areaName;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKpi() {
		return kpi;
	}
	public void setKpi(String kpi) {
		this.kpi = kpi;
	}
	public double getCount() {
		return count;
	}
	public void setCount(double count) {
		this.count = count;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getLocationId() {
		return locationId;
	}
	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public long getAreaId() {
		return areaId;
	}
	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public void setPeriodType(PeriodType periodType) {
		this.periodType = periodType;
	}
}
