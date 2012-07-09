/**
 * 
 */
package net.eatsense.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;

import net.eatsense.domain.embedded.OrderStatus;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * @author Nils Weiher
 *
 */
@Cached
public class Order extends GenericEntity<Order> {
	@Min(1)
	int amount;
	


	private boolean archived;
	
	Key<Bill> bill;
	
	@Parent
	@NotNull
	Key<Business> business;


	@NotNull
	Key<CheckIn> checkIn;
	
	String comment;


	@NotNull
	Date orderTime;
	
	@NotNull
	Key<Product> product;
	
	@NotNull
	OrderStatus status;
	
	@Unindexed
	private List<Key<OrderChoice>> choices = new ArrayList<Key<OrderChoice>>();

	public int getAmount() {
		return amount;
	}


	public Key<Bill> getBill() {
		return bill;
	}


	public Key<Business> getBusiness() {
		return business;
	}
	
	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}


	public String getComment() {
		return comment;
	}


	@Transient
	@JsonIgnore
	public Key<Order> getKey() {
		return getKey(getBusiness(), super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<Order> getKey(Key<Business> parent, Long id) {
		
		return new Key<Order>(parent, Order.class, id);
	}
	
	public Date getOrderTime() {
		return orderTime;
	}


	public Key<Product> getProduct() {
		return product;
	}


	public OrderStatus getStatus() {
		return status;
	}


	public boolean isArchived() {
		return archived;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}


	public void setArchived(boolean archived) {
		this.archived = archived;
	}


	public void setBill(Key<Bill> bill) {
		this.bill = bill;
	}


	public void setBusiness(Key<Business> business) {
		this.business = business;
	}


	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}


	public void setProduct(Key<Product> product) {
		this.product = product;
	}


	public void setStatus(OrderStatus status) {
		this.status = status;
	}


	public List<Key<OrderChoice>> getChoices() {
		return choices;
	}


	public void setChoices(List<Key<OrderChoice>> choices) {
		this.choices = choices;
	}

}
