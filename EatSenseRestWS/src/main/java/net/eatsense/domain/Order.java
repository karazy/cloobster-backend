/**
 * 
 */
package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

/**
 * @author Nils Weiher
 *
 */
public class Order extends GenericEntity {
	@NotNull
	Date timeOfPlacement;
	
	@NotNull
	Key<Product> product;
	
	@Min(1)
	int amount;
	
	
	public Date getTimeOfPlacement() {
		return timeOfPlacement;
	}


	public void setTimeOfPlacement(Date timeOfPlacement) {
		this.timeOfPlacement = timeOfPlacement;
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


	@Parent
	Key<CheckIn> checkIn;
}
