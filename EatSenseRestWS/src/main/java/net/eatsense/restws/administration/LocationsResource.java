package net.eatsense.restws.administration;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import net.eatsense.controller.LocationController;
import net.eatsense.representation.LocationProfileDTO;

public class LocationsResource {
	private final LocationController ctrl;

	@Inject
	public LocationsResource(LocationController ctrl) {
		super();
		this.ctrl = ctrl;
	}
	
	@GET
	@Produces("application/json")
	public List<LocationProfileDTO> getLocations(@QueryParam("id") long companyId) {
		return Lists.transform(ctrl.getLocations(companyId), LocationProfileDTO.toDTO);
	}
}
