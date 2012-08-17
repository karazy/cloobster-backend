package net.eatsense.restws.business;

import java.util.Collection;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.BusinessController;
import net.eatsense.controller.RequestController;
import net.eatsense.domain.Business;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.representation.RequestDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class RequestsResource {
	@Context
	private ResourceContext resourceContext;
	
	private BusinessController businessCtrl;
	private final RequestController requestCtrl;

	private Business business;

	@Inject
	public RequestsResource(BusinessController checkInController, RequestController requestCtrl) {
		this.businessCtrl = checkInController;
		this.requestCtrl = requestCtrl;
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({ Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER })
	public Iterable<RequestDTO> getCustomerRequest(
			@QueryParam("checkInId") long checkInId,
			@QueryParam("spotId") long spotId,
			@QueryParam("areaId") long areaId,
			@QueryParam("type") Set<RequestType> types) {
		return requestCtrl.getRequests(business.getKey(), areaId, spotId,
				checkInId, types);
	}

	@DELETE
	@Path("{requestId}")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteCustomerRequest(@PathParam("requestId") Long requestId) {
		try {
			businessCtrl.deleteCustomerRequest(business, requestId);
		} catch (IllegalArgumentException e) {
			throw new NotFoundException();
		}
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
}
