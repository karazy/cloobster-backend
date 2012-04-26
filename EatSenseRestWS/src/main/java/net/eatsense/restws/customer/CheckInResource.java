package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import net.eatsense.controller.BusinessController;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.CheckIn;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.ResourceContext;

public class CheckInResource {
	
	private boolean authenticated = false;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Context
	private ResourceContext resourceContext;

	private CheckIn checkIn;

	private final Provider<CheckInController> checkInCtrlprovider;
	private final Provider<BusinessController> businessCtrlProvider;
	private final Provider<ChannelController> channelCtrlProvider;
	private final Provider<OrderController> orderCtrlProvider;

	public void setCheckIn(CheckIn checkIn) {
		this.checkIn = checkIn;
	}

	@Inject
	public CheckInResource(Provider<CheckInController> checkInController,
			Provider<BusinessController> businessCtrl,
			Provider<ChannelController> channelCtrl,
			Provider<OrderController> orderCtrlProvider) {
		super();
		this.orderCtrlProvider = orderCtrlProvider;
		this.checkInCtrlprovider = checkInController;
		this.businessCtrlProvider = businessCtrl;
		this.channelCtrlProvider = channelCtrl;
	}

	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public CheckInDTO updateCheckIn( CheckInDTO checkInData) {
		if(authenticated)
			return checkInCtrlprovider.get().updateCheckIn(checkIn, checkInData);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO getCheckIn() {
		return checkInCtrlprovider.get().toDto(checkIn);
	}
	
	@DELETE
	@RolesAllowed({"guest"})
	public void deleteCheckIn() {
		if(authenticated)
			checkInCtrlprovider.get().checkOut(checkIn);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@POST
	@Path("requests")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public CustomerRequestDTO postRequest(CustomerRequestDTO requestData) {
		if(authenticated)
			return businessCtrlProvider.get().saveCustomerRequest( checkIn, requestData);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@POST
	@Path("tokens")
	@Produces("text/plain; charset=UTF-8")
	@RolesAllowed({"guest"})
	public String requestToken() {
		return channelCtrlProvider.get().createCustomerChannel(checkIn);
	}
	
	@PUT
	@Path("cart")
	@RolesAllowed({"guest"}) 
	public void updateAllCartOrders() {
		if(authenticated)
			orderCtrlProvider.get().updateCartOrdersToPlaced(checkIn);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	
}
