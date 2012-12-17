package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Id;

import net.eatsense.domain.embedded.SubscriptionStatus;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class Subscription {
	@Parent
	private Key<Business> business;
	
	/**
	 * Name for this subscription package.
	 */
	@Id
	private String name;
	
	@Unindexed
	private int maxSpotCount;
	
	private boolean quotaExceeded;
	
	@Unindexed
	private boolean basic;
	
	@Unindexed
	private long fee;
	
	private SubscriptionStatus status;
	
	private Date startDate;
	private Date endData;
	
	public Key<Business> getBusiness() {
		return business;
	}
	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMaxSpotCount() {
		return maxSpotCount;
	}
	public void setMaxSpotCount(int maxSpotCount) {
		this.maxSpotCount = maxSpotCount;
	}
	public boolean isBasic() {
		return basic;
	}
	public void setBasic(boolean basic) {
		this.basic = basic;
	}
	public long getFee() {
		return fee;
	}
	public void setFee(long fee) {
		this.fee = fee;
	}
	public SubscriptionStatus getStatus() {
		return status;
	}
	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndData() {
		return endData;
	}
	public void setEndData(Date endData) {
		this.endData = endData;
	}
	public boolean isQuotaExceeded() {
		return quotaExceeded;
	}
	public void setQuotaExceeded(boolean quotaExceeded) {
		this.quotaExceeded = quotaExceeded;
	}
}
