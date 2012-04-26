package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.representation.OrderDTO;

public class OrderEvent {
	private Business business;
	private Order order;
	private CheckIn checkIn;
	private Optional<OrderDTO> orderData;
			
	public OrderEvent(Business business, Order order, CheckIn checkIn) {
		super();
		this.business = business;
		this.order = order;
		this.checkIn = checkIn;
		this.orderData = Optional.absent();
	}
	public Optional<OrderDTO> getOrderData() {
		return orderData;
	}
	public void setOrderData(OrderDTO orderData) {
		this.orderData = Optional.fromNullable(orderData);
	}
	public Business getBusiness() {
		return business;
	}
	public Order getOrder() {
		return order;
	}
	public CheckIn getCheckIn() {
		return checkIn;
	}
	
}
