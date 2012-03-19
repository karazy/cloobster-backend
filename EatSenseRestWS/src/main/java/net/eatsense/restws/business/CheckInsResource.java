package net.eatsense.restws.business;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.CheckInController;
import net.eatsense.representation.cockpit.CheckInStatusDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class CheckInsResource {
	private CheckInController checkInController;
	
	@Inject
	public CheckInsResource(CheckInController checkInController) {
		super();
		this.checkInController = checkInController;
	}

	@Context
	private ResourceContext resourceContext;
	
	private Long businessId;
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<CheckInStatusDTO> getCheckIns(@QueryParam("spotId") Long spotId) {
		return checkInController.getCheckInStatusesBySpot(businessId, spotId);	
	}
	

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
}
