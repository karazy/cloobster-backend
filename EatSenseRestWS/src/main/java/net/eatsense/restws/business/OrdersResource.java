package net.eatsense.restws.business;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.auth.Role;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.domain.Order;
import net.eatsense.representation.OrderDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class OrdersResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private OrderController orderController;
	private Business business;
	
	public void setBusiness(Business business) {
		this.business = business;
	}

	@Inject
	public OrdersResource(OrderController orderController) {
		super();
		this.orderController = orderController;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Collection<OrderDTO> getOrders(@QueryParam("spotId") Long spotId, @QueryParam("checkInId") Long checkInId) {
		return orderController.getOrdersBySpotAsDto(business, spotId, checkInId);
	}
	
	@Path("{id}")
	public OrderResource getOrderResource(@PathParam("id") Long orderId) {
		Order order = orderController.getOrder(business, orderId);
		if( order == null)
			throw new NotFoundException();
		OrderResource orderResource = resourceContext.getResource(OrderResource.class);
		orderResource.setBusiness(business);
		orderResource.setOrder(order);
		return orderResource;
	}
}
