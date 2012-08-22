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

import net.eatsense.HttpMethods;
import net.eatsense.auth.Role;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.CheckInController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.User;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.CheckInDTO;

import com.google.common.base.Optional;
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
	private final BusinessRepository businessRepo;	
	
	@Inject
	public CheckInsResource(CheckInController checkInCtr, Provider<ChannelController> channelCtrl, BusinessRepository businessRepo) {
		super();
		this.channelCtrlProvider = channelCtrl;
		this.checkInCtrl = checkInCtr;
		this.businessRepo = businessRepo;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CheckInDTO createCheckIn(CheckInDTO checkIn) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return checkInCtrl.createCheckIn(checkIn, Optional.fromNullable(account));
		
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
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
	public CheckInResource getCheckInResource(@PathParam("checkInUid") String checkInUid) {
		CheckIn checkInFromPath = checkInCtrl.getCheckIn(checkInUid);
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		// Check that the authenticated checkin owns the entity
		boolean authenticated = checkIn== null ? false : checkInFromPath.getId().equals(checkIn.getId());
		
		if(HttpMethods.WRITE_METHODS.contains(servletRequest.getMethod())) {
			// Check for read-only mode.
			Business business = businessRepo.getByKey(checkIn.getBusiness());
			if(business.isTrash())
				throw new IllegalAccessException("Business for this CheckIn has been deleted.");
		}
		
		CheckInResource checkInResource = resourceContext.getResource(CheckInResource.class);
		checkInResource.setCheckIn(checkInFromPath);
		checkInResource.setAccount((Account)servletRequest.getAttribute("net.eatsense.domain.Account"));
		checkInResource.setAuthenticated(authenticated);
		
		return checkInResource;
	}

}
