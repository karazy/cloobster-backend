package net.eatsense.restws.customer;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.ChannelController;
import net.eatsense.controller.CheckInController;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.User;
import net.eatsense.representation.CheckInDTO;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.ResourceContext;

@Path("c/checkins")
public class CheckInsResource {
	private final CheckInController checkInCtrl;
	private final Provider<ChannelController> channelCtrlProvider;
	
	@Context
	private ResourceContext resourceContext;
	@Context
	HttpServletRequest servletRequest;
	
	@Inject
	public CheckInsResource(CheckInController checkInCtr, Provider<ChannelController> channelCtrl) {
		super();
		this.channelCtrlProvider = channelCtrl;
		this.checkInCtrl = checkInCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO createCheckIn(CheckInDTO checkIn) {
		return checkInCtrl.createCheckIn(checkIn);
		
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public Collection<User> getUsersAtSpot(@QueryParam("spotId") String spotBarcode, @QueryParam("checkInId") String checkInId) {
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		return checkInCtrl.getOtherUsersAtSpot(checkIn, spotBarcode);
	}
	
	@GET
	@Path("channels")
	public String getConnectionStatus(@QueryParam ("c") String checkInUid) {
		return channelCtrlProvider.get().checkOnlineStatusOfCheckIn(checkInUid);
	}

	
	@Path("{checkInUid}")
	@RolesAllowed({"guest"})
	public CheckInResource getCheckInResource(@PathParam("checkInUid") String checkInUid) {
		CheckIn checkInFromPath = checkInCtrl.getCheckIn(checkInUid);
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		// Check that the authenticated checkin owns the entity
		boolean authenticated = checkIn== null ? false : checkInFromPath.getId().equals(checkIn.getId());

		CheckInResource checkInResource = resourceContext.getResource(CheckInResource.class);
		checkInResource.setCheckIn(checkInFromPath);

		checkInResource.setAuthenticated(authenticated);
		
		return checkInResource;
	}

}
