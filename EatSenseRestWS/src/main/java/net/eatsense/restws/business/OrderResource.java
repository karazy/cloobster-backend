package net.eatsense.restws.business;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.domain.Order;
import net.eatsense.representation.OrderDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class OrderResource {
	@Inject
	public OrderResource(OrderController orderController) {
		super();
		this.orderController = orderController;
	}
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private OrderController orderController;
	
	private Order order;
	private Business business;

	public void setOrder(Order order) {
		this.order = order;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO updateOrder(OrderDTO orderData) {
		return orderController.updateOrderForBusiness(business, order, orderData);
	}
}
