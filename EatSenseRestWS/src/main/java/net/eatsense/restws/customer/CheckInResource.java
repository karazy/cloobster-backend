package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

import net.eatsense.controller.BusinessController;
import net.eatsense.controller.CheckInController;
import net.eatsense.domain.CheckIn;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;

public class CheckInResource {
	
	private boolean authenticated = false;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Context
	private ResourceContext resourceContext;

	private CheckIn checkIn;

	private CheckInController checkInCtrl;

	private BusinessController businessCtrl;

	public void setCheckIn(CheckIn checkIn) {
		this.checkIn = checkIn;
	}

	@Inject
	public CheckInResource(CheckInController checkInController,  BusinessController businessCtrl) {
		super();
		this.checkInCtrl = checkInController;
		this.businessCtrl = businessCtrl;
	}
	
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public CheckInDTO updateCheckIn( CheckInDTO checkInData) {
		if(authenticated)
			return checkInCtrl.updateCheckIn(checkIn, checkInData);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO getCheckIn() {
		return checkInCtrl.toDto(checkIn);
	}
	
	@DELETE
	@RolesAllowed({"guest"})
	public void deleteCheckIn() {
		if(authenticated)
			checkInCtrl.checkOut(checkIn);
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
			return businessCtrl.saveCustomerRequest(checkIn, requestData);
		else
			throw new WebApplicationException(Status.FORBIDDEN);
	}
	
	@POST
	@Path("tokens")
	@Produces("text/plain; charset=UTF-8")
	@RolesAllowed({"guest"})
	public String requestToken() {
		return checkInCtrl.requestToken(checkIn);
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	
}
