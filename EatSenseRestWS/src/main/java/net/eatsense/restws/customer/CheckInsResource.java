package net.eatsense.restws.customer;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

import net.eatsense.controller.BusinessController;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.CheckInController;
import net.eatsense.domain.User;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;

@Path("c/checkins")
public class CheckInsResource {
	private CheckInController checkInCtrl;
	private BusinessController businessCtrl;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	@Inject
	public CheckInsResource(CheckInController checkInCtr, BusinessController businessCtrl) {
		super();
		this.checkInCtrl = checkInCtr;
		this.businessCtrl = businessCtrl;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
//	@Produces("text/plain; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO createCheckIn(CheckInDTO checkIn) {
//		String userId = checkInCtr.createCheckIn(checkIn);
		CheckInDTO userId = checkInCtrl.createCheckIn(checkIn);
		return userId;
	}
	
	@PUT
	@Path("{checkInId}")
	@Consumes("application/json; charset=UTF-8")
//	@Produces("text/plain; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO updateCheckIn(@PathParam("checkInId")String checkInId, CheckInDTO checkIn) {
		return checkInCtrl.updateCheckIn(checkInId, checkIn);
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<User> getUsersAtSpot(@QueryParam("spotId") String spotId, @QueryParam("checkInId") String checkInId) { 
		return checkInCtrl.getUsersAtSpot(spotId,checkInId);
	}
	
	@GET
	@Path("{checkInId}")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO getCheckIn(@PathParam("checkInId") String checkInId) {
		CheckInDTO checkIn = checkInCtrl.getCheckInAsDTO(checkInId);
		if(checkIn == null)
			throw new NotFoundException();
		return checkIn;
	}
	
	@DELETE
	@Path("{checkInId}")
	public void deleteCheckIn(@PathParam("checkInId") String checkInId) {
		checkInCtrl.checkOut(checkInId);
	}
	
	@POST
	@Path("{checkInId}/requests")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CustomerRequestDTO postRequest(@PathParam("checkInId") String checkInId, CustomerRequestDTO requestData) {
		return businessCtrl.saveCustomerRequest(checkInId, requestData);
	}
	
	@POST
	@Path("{checkInUid}/tokens")
	@Produces("text/plain; charset=UTF-8")
	public String requestToken(@PathParam("checkInUid") String checkInUid) {
		try {
			return checkInCtrl.requestToken(checkInUid);
		} catch (IllegalArgumentException e) {
			logger.error("Failed channel creation", e);
			throw new NotFoundException(e.getMessage());
		}
	}
	
}
