package net.eatsense.restws.business;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.CheckInController;
import net.eatsense.representation.cockpit.CheckInStatusDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class CheckInsResource {
	private CheckInController checkInController;
	@Context
	private ResourceContext resourceContext;
	
	private Long businessId;
	
	@Inject
	public CheckInsResource(CheckInController checkInController) {
		super();
		this.checkInController = checkInController;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<CheckInStatusDTO> getCheckIns(@QueryParam("spotId") Long spotId) {
		return checkInController.getCheckInStatusesBySpot(businessId, spotId);	
	}
	
	@DELETE
	@Path("{checkInId}")
	public void cancelAndDeleteCheckIn(@PathParam("checkInId") Long checkInId) {
		checkInController.deleteCheckIn(checkInId);
	}
	
}
