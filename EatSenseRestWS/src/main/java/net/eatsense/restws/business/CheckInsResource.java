package net.eatsense.restws.business;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.representation.AccountForServiceDTO;
import net.eatsense.representation.CheckInHistoryDTO;
import net.eatsense.representation.cockpit.CheckInStatusDTO;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.ResourceContext;

public class CheckInsResource {
	private CheckInController checkInController;
	@Context
	private ResourceContext resourceContext;
	
	private Business business;
	private final Provider<OrderController> orderCtrlProvider;
	
	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@Inject
	public CheckInsResource(CheckInController checkInController, Provider<OrderController> orderControllerProvider) {
		super();
		this.orderCtrlProvider = orderControllerProvider;
		this.checkInController = checkInController;
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Iterable<CheckInStatusDTO> getCheckIns(@QueryParam("spotId") Long spotId, @QueryParam("inactive") boolean inactive) {
		if(inactive) {
			return Iterables.transform(checkInController.getInactiveCheckIns(business.getKey()),CheckInStatusDTO.toDTO);
		}
		return checkInController.getCheckInStatusesBySpot(business, spotId);	
	}
	
	/**
	 * Return none senstive account information to help business fullfill its service.
	 * @return
	 */
	@GET
	@Path("{checkInId}/account")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public AccountForServiceDTO getAccount(@PathParam("checkInId") Long checkInId) {
		return checkInController.getAccountByCheckInId(checkInId);
	}
	
	
	/**
	 * @param areaId
	 * @param start Offset for pagination.
	 * @param limit Limit per query for pagination.
	 * @return List of past completed check ins with bill and spot data.
	 */
	@GET
	@Path("history")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Collection<CheckInHistoryDTO> getCheckInHistory(@QueryParam("areaId") long areaId, @QueryParam("start") int start, @QueryParam("limit") int limit) {
		return checkInController.getHistory(business.getKey(), areaId, start, limit);
	}
	
	
	@DELETE
	@Path("{checkInId}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CheckInStatusDTO cancelAndDeleteCheckIn(@PathParam("checkInId") Long checkInId) {
		return checkInController.deleteCheckIn(business, checkInId);
	}
	
	@PUT
	@Path("{checkInId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CheckInStatusDTO updateCheckin(@PathParam("checkInId") Long checkInId, CheckInStatusDTO checkInData) {
		return checkInController.updateCheckInAsBusiness(business, checkInId, checkInData);
	}
	
	@PUT
	@Path("{checkInId}/cart")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void updateCheckInOrders(@PathParam("checkInId") long checkInId) {
		orderCtrlProvider.get().confirmPlacedOrdersForCheckIn(business, checkInId);
	}
}
