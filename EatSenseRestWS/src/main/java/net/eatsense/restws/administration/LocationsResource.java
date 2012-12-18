package net.eatsense.restws.administration;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import net.eatsense.controller.LocationController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.SubscriptionDTO;

public class LocationsResource {
	private final LocationController ctrl;
	private final SubscriptionController subCtrl;

	@Inject
	public LocationsResource(LocationController ctrl, SubscriptionController subCtrl) {
		super();
		this.ctrl = ctrl;
		this.subCtrl = subCtrl;
	}
	
	@GET
	@Produces("application/json")
	public List<LocationProfileDTO> getLocations(@QueryParam("companyId") long companyId) {
		return Lists.transform(ctrl.getLocations(companyId), LocationProfileDTO.toDTO);
	}
	
	@POST
	@Path("{locationId}/subscriptions")
	public SubscriptionDTO createNewSubscription(@PathParam("locationId") long locationId, SubscriptionDTO subscriptionData) {
		return new SubscriptionDTO(subCtrl.createSubscriptionFromTemplate(subCtrl.getPackage(subscriptionData.getTemplateId()), subscriptionData.getStatus(), locationId, true));
	}
}
