package net.eatsense.restws.business;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.controller.OrderController;
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
	private Long businessId;
	private Long orderId;
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO updateOrder(OrderDTO orderData) {
		return orderController.updateOrderForBusiness(businessId, orderId, orderData);
	}
}
