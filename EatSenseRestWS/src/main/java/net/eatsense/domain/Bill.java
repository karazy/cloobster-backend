package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.PaymentMethod;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class Bill extends GenericEntity<Bill> {
	@Parent
	@NotNull
	private Key<Business> business;
	
	@NotNull
	private Key<CheckIn> checkIn;
	
	@NotNull
	private Date creationTime;
	
	@Embedded
	@NotNull
	@Valid
	@Unindexed
	private PaymentMethod paymentMethod;
	
	@NotNull
	@Min(0)
	private Long total;
	
	private boolean cleared;

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		this.business = business;
	}

	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
	
	@Transient
	public Key<Bill> getKey() {
		return new Key<Bill>(getBusiness(), Bill.class, super.getId());
	}

	public boolean isCleared() {
		return cleared;
	}

	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}

}
