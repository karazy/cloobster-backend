/**
 * 
 */
package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * @author Nils Weiher
 *
 */
public class Order extends GenericEntity {
	@NotNull
	Date orderTime;
	


	@NotNull
	Key<Product> product;
	
	@Min(1)
	int amount;
	
	String comment;


	@Parent
	@NotNull
	Key<Restaurant> restaurant;
	
	public Key<Restaurant> getRestaurant() {
		return restaurant;
	}


	public void setRestaurant(Key<Restaurant> restaurant) {
		this.restaurant = restaurant;
	}


	@NotNull
	Key<CheckIn> checkIn;
	
	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public Date getOrderTime() {
		return orderTime;
	}


	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public Key<Product> getProduct() {
		return product;
	}


	public void setProduct(Key<Product> product) {
		this.product = product;
	}


	public int getAmount() {
		return amount;
	}


	public void setAmount(int amount) {
		this.amount = amount;
	}


	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}


	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}
	
	@Transient
	public Key<Order> getKey() {
		return new Key<Order>(getRestaurant(), Order.class, super.getId());
	}

}
