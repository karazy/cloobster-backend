package net.eatsense.counter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

public class Counter {
	@Id
	private String id;
	private String name;
	private Long locationId;
	private Long areaId;
	private long count;
	private PeriodType periodType;
	private String period;
	
	public enum PeriodType {
		SECOND {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(period);
			}
		},
		MINUTE {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(period);
			}
		},
		HOUR {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy-MM-dd HH").format(period);
			}
		},
		DAY {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy-MM-dd").format(period);
			}
		},
		WEEK {
			@Override
			public String getScope(Date period) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(period);
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				return new SimpleDateFormat("yyyy-MM-dd'week'").format(calendar.getTime());
			}
		},
		MONTH {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy-MM").format(period);
			}
		},
		YEAR {
			@Override
			public String getScope(Date period) {
				return new SimpleDateFormat("yyyy").format(period);
			}
		},
		ALL {
			@Override
			public String getScope(Date period) {
				return "ALL";
			}
		};
		
		public abstract String getScope(Date period);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public Long getAreaId() {
		return areaId;
	}
	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public PeriodType getPeriodType() {
		return periodType;
	}
	public void setPeriodType(PeriodType periodType) {
		this.periodType = periodType;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
}
