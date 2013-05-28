package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.auth.Role;
import net.eatsense.controller.VisitController;
import net.eatsense.domain.Account;
import net.eatsense.representation.ToVisitDTO;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@Path("/c/visits")
@Produces("application/json; charset=UTF-8")
@RolesAllowed(Role.USER)
public class VisitsResource {
	private Account account;
	private final VisitController visitCtrl;
	
	@Inject
	public VisitsResource(VisitController visitCtrl, HttpServletRequest servletRequest) {
		this.visitCtrl = visitCtrl;
		this.account = (Account) servletRequest.getAttribute("net.eatsense.domain.Account");
	}
	
	@GET
	public Iterable<ToVisitDTO> getVisits(@QueryParam("limit") int limit,@QueryParam("start") int start) {
		return Iterables.transform(visitCtrl.getVisitsSorted(account, start, limit), ToVisitDTO.toDTO);
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	public ToVisitDTO createVisit(ToVisitDTO visitData) {
		return new ToVisitDTO(visitCtrl.createVisit(account, visitData));
	}
	
	@GET
	@Path("{visitId}")
	public ToVisitDTO getVisit(@PathParam("visitId") long visitId) {
		return new ToVisitDTO(visitCtrl.getVisit(account, visitId));
	}
	
	@PUT
	@Path("{visitId}")
	@Consumes("application/json; charset=UTF-8")
	public ToVisitDTO updateVisit(@PathParam("visitId") long visitId, ToVisitDTO visitData) {
		return new ToVisitDTO(visitCtrl.getAndUpdateVisit(account, visitId, visitData));
	}
	
	@DELETE
	@Path("{visitId}")
	public void deleteVisit(@PathParam("visitId") long visitId) {
		visitCtrl.deleteVisit(account, visitId);
	}
	
	@DELETE
	@Path("{visitId}/image")
	public void deleteVisitImage(@PathParam("visitId") long visitId) {
		visitCtrl.deleteVisitImage(account, visitId);
	}
	
}
