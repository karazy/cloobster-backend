package net.eatsense.restws.administration;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import net.eatsense.controller.ChannelController;
import net.eatsense.controller.LocationController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.representation.ChannelDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.SubscriptionDTO;

public class LocationsResource {
	private final LocationController ctrl;
	private final SubscriptionController subCtrl;
	private final ChannelController channelController;

	@Inject
	public LocationsResource(LocationController ctrl, SubscriptionController subCtrl, ChannelController channelController) {
		super();
		this.ctrl = ctrl;
		this.subCtrl = subCtrl;
		this.channelController = channelController;
	}
	
	@GET
	@Produces("application/json")
	public List<LocationProfileDTO> getLocations(@QueryParam("companyId") long companyId) {
		return Lists.transform(ctrl.getLocations(companyId), LocationProfileDTO.toDTO);
	}
	
	@GET
	@Path("{locationId}")
	@Produces("application/json")
	public LocationProfileDTO getLocation(@PathParam("locationId") long locationId) {
		return new LocationProfileDTO(ctrl.get(locationId, true));
	}
	
	@POST
	@Path("{locationId}/subscriptions")
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO createNewSubscription(@PathParam("locationId") long locationId, SubscriptionDTO subscriptionData) {
		return new SubscriptionDTO(subCtrl.createAndSetSubscription(subscriptionData.getTemplateId(), subscriptionData.getStatus(), locationId));
	}
	
	@GET
	@Path("{locationId}/subscriptions")
	@Consumes("application/json")
	@Produces("application/json")
	public Iterable<SubscriptionDTO> getSubscriptionsForLocation(@PathParam("locationId") long locationId) {
		return Iterables.transform(subCtrl.get(locationId), SubscriptionDTO.toDTO);
	}
	
	@GET
	@Path("{locationId}/subscriptions/{subscriptionId}")
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO getSubscription(@PathParam("locationId") long locationId, @PathParam("subscriptionId") long subscriptionId) {
		return new SubscriptionDTO(subCtrl.get(locationId, subscriptionId));
	}
	
	@PUT
	@Path("{locationId}/subscriptions/{subscriptionId}")
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO updateSubscription(@PathParam("locationId") long locationId, @PathParam("subscriptionId") long subscriptionId, SubscriptionDTO subscriptionData) {
		return new SubscriptionDTO(subCtrl.getAndUpdateSubcription(locationId, subscriptionId, subscriptionData));
	}
	
	@POST
	@Path("{locationId}/channels/{clientId}")
	@Consumes("application/json")
	@Produces("application/json")
	public ChannelDTO sendChannelWarning(@PathParam("locationId") long locationId, @PathParam("clientId") String clientId) {
		return new ChannelDTO(channelController.getAndPostChannelWarningEvent(locationId, clientId));
	}
	
	@DELETE
	@Path("{locationId}/channels/{clientId}")
	public void removeChannel(@PathParam("locationId") long locationId, @PathParam("clientId") String clientId) {
		channelController.removeChannelTracking(locationId, clientId);
	}
}
