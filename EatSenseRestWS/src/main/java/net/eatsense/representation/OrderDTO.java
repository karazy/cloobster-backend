package net.eatsense.representation;

import java.util.Collection;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.OrderStatus;

@XmlRootElement
public class OrderDTO {
	Long id;
	@Min(1)
	int amount;
	OrderStatus status;
	String comment;
	
	Date orderTime;
	
	/**
	 * CheckIn Id this order is assigned to.
	 */
	private Long checkInId;
	
	private String productName;
	private Long productId;
	private String productShortDesc;
	private String productLongDesc;
	private double productPrice;
	private String productImageUrl;
	
	private Collection<ChoiceDTO> choices;
	
	public OrderDTO(Order order) {
		super();
		if(order == null)
			return;
		
		id = order.getId();
		amount = order.getAmount();
		status = order.getStatus();
		comment = order.getComment();
		orderTime = order.getOrderTime();
		
		if(order.getCheckIn() != null) {
			checkInId = order.getCheckIn().getId();
		}
		
		if(order.getProduct() != null) {
			productId = order.getProduct().getId();
		}
		else {
			return;
		}
		
		productName = order.getProductName();
		productShortDesc = order.getProductShortDesc();
		productLongDesc = order.getProductLongDesc();
		productPrice = order.getProductPrice() / 100d;
		productImageUrl = order.getProductImage() != null ? order.getProductImage().getUrl() : null;
	}
	
	public OrderDTO() {
		super();
	}

	public Date getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
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
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductShortDesc() {
		return productShortDesc;
	}
	public void setProductShortDesc(String productShortDesc) {
		this.productShortDesc = productShortDesc;
	}
	public String getProductLongDesc() {
		return productLongDesc;
	}
	public void setProductLongDesc(String productLongDesc) {
		this.productLongDesc = productLongDesc;
	}
	public double getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(double productPrice) {
		this.productPrice = productPrice;
	}
	public Collection<ChoiceDTO> getChoices() {
		return choices;
	}
	public void setChoices(Collection<ChoiceDTO> choices) {
		this.choices = choices;
	}

	public String getProductImageUrl() {
		return productImageUrl;
	}

	public void setProductImageUrl(String productImageUrl) {
		this.productImageUrl = productImageUrl;
	}
}
