package net.eatsense.restws.business;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.OrderController;
import net.eatsense.representation.OrderDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class OrdersResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private OrderController orderController;
	private Long businessId;
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	@Inject
	public OrdersResource(OrderController orderController) {
		super();
		this.orderController = orderController;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<OrderDTO> getOrders(@QueryParam("spotId") Long spotId, @QueryParam("checkInId") Long checkInId) {
		logger.debug("retrieving orders");
		return orderController.getOrdersBySpotAsDto(businessId, spotId, checkInId);
	}
}
