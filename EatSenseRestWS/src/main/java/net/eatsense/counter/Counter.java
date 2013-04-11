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
	
	@Unindexed
	private long count;
	
	private PeriodType periodType;
	private Date period;
	
	public Counter() {
	}
	
	public Counter(Counter counter) {
		super();
		this.id = counter.getId();
		this.name = counter.getName();
		this.locationId = counter.getLocationId();
		this.areaId = counter.getAreaId();
		this.count = counter.getCount();
		this.periodType = counter.getPeriodType();
		this.period = counter.getPeriod();
	}
	
	public Counter(String id, String name, Long locationId, Long areaId,
			long count, PeriodType periodType, Date period) {
		super();
		this.id = id;
		this.name = name;
		this.locationId = locationId;
		this.areaId = areaId;
		this.count = count;
		this.periodType = periodType;
		this.period = period;
	}

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
	public Date getPeriod() {
		return period;
	}
	public void setPeriod(Date period) {
		this.period = period;
	}
}
