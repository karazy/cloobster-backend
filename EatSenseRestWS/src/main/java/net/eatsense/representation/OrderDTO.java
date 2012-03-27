package net.eatsense.representation;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.OrderStatus;

public class OrderDTO {
	Long id;
	@Min(1)
	int amount;
	OrderStatus status;
	String comment;
	
	@NotNull
	@Valid
	ProductDTO product;
	
	Date orderTime;
	
	/**
	 * CheckIn Id this order is assigned to.
	 */
	private Long checkInId;
	
	public Date getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}
	public ProductDTO getProduct() {
		return product;
	}
	public void setProduct(ProductDTO product) {
		this.product = product;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public OrderStatus getStatus() {
		return status;
	}
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Long getCheckInId() {
		return checkInId;
	}
	public void setCheckInId(Long checkInId) {
		this.checkInId = checkInId;
	}
	
	
}
