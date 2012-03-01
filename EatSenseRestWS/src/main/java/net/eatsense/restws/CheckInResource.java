package net.eatsense.restws;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.inject.Inject;

import net.eatsense.controller.CheckInController;
import net.eatsense.domain.User;
import net.eatsense.representation.CheckInDTO;

@Path("checkins")
public class CheckInResource {
	private CheckInController checkInCtr;

	@Inject
	public CheckInResource(CheckInController checkInCtr) {
		super();
		this.checkInCtr = checkInCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
//	@Produces("text/plain; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO createCheckIn(CheckInDTO checkIn) {
//		String userId = checkInCtr.createCheckIn(checkIn);
		CheckInDTO userId = checkInCtr.createCheckIn(checkIn);
		return userId;
	}
	
	@PUT
	@Path("{checkInId}")
	@Consumes("application/json; charset=UTF-8")
//	@Produces("text/plain; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO updateCheckIn(@PathParam("checkInId")String checkInId, CheckInDTO checkIn) {
		return checkInCtr.updateCheckIn(checkInId, checkIn);
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<User> getUsersAtSpot(@QueryParam("spotId") String spotId, @QueryParam("checkInId") String checkInId) { 
		return checkInCtr.getUsersAtSpot(spotId,checkInId);
	}
	
	@GET
	@Path("{checkInId}")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO getCheckIn(@PathParam("checkInId") String checkInId) {
		
		return null;
	}
	
}
