package net.eatsense.restws.customer;

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.HistoryStatusDTO;
import net.eatsense.representation.VisitDTO;

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
	private final LocationRepository businessRepo;	
	
	@Inject
	public CheckInsResource(CheckInController checkInCtr, Provider<ChannelController> channelCtrl, LocationRepository businessRepo) {
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
	
	/**
	 * Get previous visits for this account.
	 * 
	 * @param installId
	 * @return Array of visit objects, containing check in , bill and business data.
	 */
	@GET
	@Path("history")
	@Produces("application/json; charset=UTF-8")
	public List<VisitDTO> getVisits(@QueryParam("installId") String installId, @QueryParam("start") int start, @QueryParam("limit") int limit ) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		
		return checkInCtrl.getVisits(Optional.fromNullable(account), installId , start, limit);
	}
	
	@PUT
	@Path("history/connect")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.USER)
	public HistoryStatusDTO connectVisits(HistoryStatusDTO historyDTO, @QueryParam("installId") String installId) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return checkInCtrl.connectVisits(account, historyDTO);
	}
	
	@GET
	@Path("channels")
	public String getConnectionStatus(@QueryParam ("c") String checkInUid) {
		return channelCtrlProvider.get().checkOnlineStatusOfCheckIn(checkInUid);
	}

	
	@Path("{checkInUid}")
	public CheckInResource getCheckInResource(@PathParam("checkInUid") String checkInUid) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		CheckIn checkInFromPath = checkInCtrl.getCheckIn(checkInUid);
		
		if(checkInFromPath == null) {
			throw new NotFoundException();
		}
		
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		// Check that the authenticated checkin owns the entity
		boolean authenticated = false;
		if(checkIn != null && checkInFromPath.getId().equals(checkIn.getId())) {
			authenticated = true;
		}
		if (!authenticated
				&& account != null
				&& checkInFromPath.getAccount() != null
				&& checkInFromPath.getAccount().getId() == account.getId().longValue()) {
			// CheckIn belongs to authenticated Account
			authenticated = true;
		}

		if(HttpMethods.WRITE_METHODS.contains(servletRequest.getMethod())) {
			// Check for read-only mode.
			Business business = businessRepo.getByKey(checkIn.getBusiness());
			if(business.isTrash())
				throw new IllegalAccessException("Business for this CheckIn has been deleted.");
		}
		
		CheckInResource checkInResource = resourceContext.getResource(CheckInResource.class);
		checkInResource.setCheckIn(checkInFromPath);
		
		checkInResource.setAccount(account);
		checkInResource.setAuthenticated(authenticated);
		
		return checkInResource;
	}
}
