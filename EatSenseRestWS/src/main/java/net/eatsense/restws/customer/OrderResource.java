package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.eatsense.auth.Role;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.representation.OrderDTO;

import com.google.inject.Inject;

public class OrderResource {
	private OrderController orderCtrl;
	private Location business;
	private CheckIn checkIn;
	private Order order;
	private boolean authorized = false;
	@Inject
	public OrderResource(OrderController orderCtrl) {
		super();
		this.orderCtrl = orderCtrl;
	}
	
	public Location getBusiness() {
		return business;
	}
	public void setBusiness(Location business) {
		this.business = business;
	}
	public void setCheckIn(CheckIn checkIn) {
		this.checkIn = checkIn;
	}
	public void setOrder(Order order) {
		this.order = order;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public OrderDTO getOrder() {
		return orderCtrl.toDto(order);
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public OrderDTO updateOrder( OrderDTO orderData) {
		// Only update the order if this was an authorized request, i.e. this order belongs to the given checkin.
		if(authorized)
			return orderCtrl.updateOrder(business, order, orderData, checkIn);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@DELETE
	@RolesAllowed(Role.GUEST)
	public void deleteOrderFromCart() {
		if(authorized)
			orderCtrl.deleteOrder(business, order, checkIn);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
}
