package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class Bill extends GenericEntity {
	@Parent
	@NotNull
	private Key<Restaurant> restaurant;
	
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
	private Float total;

	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
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

	public Float getTotal() {
		return total;
	}

	public void setTotal(Float total) {
		this.total = total;
	}
	
	@Transient
	public Key<Bill> getKey() {
		return new Key<Bill>(getRestaurant(), Bill.class, super.getId());
	}

}
