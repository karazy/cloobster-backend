package net.eatsense.representation;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.validation.CreationChecks;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.base.Function;

public class SubscriptionDTO {
	private Long id;
	
	@NotNull(groups= {CreationChecks.class })
	@Min(value = 1, groups= {CreationChecks.class })
	private Long templateId;
	@NotEmpty
	@NotNull
	private String name;
	
	@Min(0)
	private int maxSpotCount;
	
	private boolean quotaExceeded;
	
	private boolean basic;
	
	private double fee;
	
	@NotNull
	private SubscriptionStatus status;
	
	private Date startDate;
	private Date endDate;
	
	private Long businessId;
	
	public SubscriptionDTO() {
		super();
	}
	
	public SubscriptionDTO(Subscription subscription) {
		this();
		this.setId(subscription.getId());
		this.basic = subscription.isBasic();
		this.businessId = subscription.getBusiness() != null ? subscription.getBusiness().getId() : null;
		this.endDate = subscription.getEndData();
		this.fee = subscription.getFee() / 100d;
		this.maxSpotCount = subscription.getMaxSpotCount();
		this.name = subscription.getName();
		this.quotaExceeded = subscription.isQuotaExceeded();
		this.startDate = subscription.getStartDate();
		this.status = subscription.getStatus();
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
	public boolean isQuotaExceeded() {
		return quotaExceeded;
	}
	public void setQuotaExceeded(boolean quotaExceeded) {
		this.quotaExceeded = quotaExceeded;
	}
	public boolean isBasic() {
		return basic;
	}
	public void setBasic(boolean basic) {
		this.basic = basic;
	}
	public double getFee() {
		return fee;
	}
	
	/**
	 * @return price*100 rounded to the closest integer
	 */
	@JsonIgnore
	public long getFeeMinor() {
		return Math.round(fee * 100);
	}
	
	public void setFee(double fee) {
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
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	/**
	 * Contains a guava transform function
	 */
	public final static Function<Subscription, SubscriptionDTO> toDTO = new Function<Subscription, SubscriptionDTO>() {
		@Override
		public SubscriptionDTO apply(Subscription input) {
			return new SubscriptionDTO(input);
		}
	};
}
