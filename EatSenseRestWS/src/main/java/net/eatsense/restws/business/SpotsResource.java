package net.eatsense.restws.business;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.BusinessController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class SpotsResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private BusinessController businessController;
	private Business business;
	
	@Inject
	public SpotsResource(BusinessController businessController) {
		super();
		this.businessController = businessController;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"restaurantadmin"})
	public Collection<SpotStatusDTO> getSpotCockpitInformation() throws Exception {
		return businessController.getSpotStatusData(business);
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
}
