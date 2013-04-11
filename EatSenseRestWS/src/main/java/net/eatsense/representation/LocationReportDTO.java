package net.eatsense.representation;

public class LocationReportDTO {
	private Long companyId;
	private Long locationId;
	private String id;
	private String companyName;
	private String locationName;
	
	private long checkInCount;
	private long orderCount;
	private long serviceCallCount;
	private long feedbackCount;
		
	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public long getCheckInCount() {
		return checkInCount;
	}
	public void setCheckInCount(long checkInCount) {
		this.checkInCount = checkInCount;
	}
	public long getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(long orderCount) {
		this.orderCount = orderCount;
	}
	public long getServiceCallCount() {
		return serviceCallCount;
	}
	public void setServiceCallCount(long serviceCallCount) {
		this.serviceCallCount = serviceCallCount;
	}
	public long getFeedbackCount() {
		return feedbackCount;
	}
	public void setFeedbackCount(long feedbackCount) {
		this.feedbackCount = feedbackCount;
	}
	
}
