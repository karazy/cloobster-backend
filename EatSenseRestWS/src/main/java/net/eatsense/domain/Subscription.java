package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import net.eatsense.domain.embedded.SubscriptionStatus;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class Subscription {
	@Parent
	private Key<Business> business;

	@Id
	private Long id;
	
	/**
	 * Name for this subscription package.
	 */
	private String name;
	
	@Unindexed
	private int maxSpotCount;
	
	private boolean quotaExceeded;
	
	private boolean basic;
	
	@Unindexed
	private long fee;
	
	private SubscriptionStatus status;
	
	private Date startDate;
	private Date endData;
	
	private boolean template;
	
	private Key<Subscription> templateKey;
	
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
	
	@Transient
	public static Key<Subscription> getKey(long id) {
		return Key.create(Subscription.class, id);
	}
	
	public  Key<Subscription> getKey() {
		if(business == null) {
			return Key.create(Subscription.class, id);
		}
		else {
			return Key.create(business, Subscription.class, id);
		}
	}
	public boolean isTemplate() {
		return template;
	}
	public void setTemplate(boolean template) {
		this.template = template;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Key<Subscription> getTemplateKey() {
		return templateKey;
	}
	public void setTemplateKey(Key<Subscription> templateKey) {
		this.templateKey = templateKey;
	}
}
