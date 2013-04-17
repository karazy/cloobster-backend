package net.eatsense.restws.administration;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import net.eatsense.controller.ChannelController;
import net.eatsense.controller.LocationController;
import net.eatsense.controller.ReportController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.management.LocationManagement;
import net.eatsense.representation.ChannelDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.LocationReportDTO;
import net.eatsense.representation.SubscriptionDTO;

public class LocationsResource {
	private final LocationController ctrl;
	private final SubscriptionController subCtrl;
	private final ChannelController channelController;
	private final ReportController reportController;
	private final LocationManagement locationManagement;
	private final UriInfo uriInfo;

	@Inject
	public LocationsResource(LocationController ctrl, SubscriptionController subCtrl, ChannelController channelController, ReportController reportController, LocationManagement locationManagement, UriInfo uriInfo) {
		super();
		this.ctrl = ctrl;
		this.subCtrl = subCtrl;
		this.channelController = channelController;
		this.reportController = reportController;
		this.locationManagement = locationManagement;
		this.uriInfo = uriInfo;
	}
	
	@GET
	@Produces("application/json")
	public List<LocationProfileDTO> getLocations(@QueryParam("companyId") long companyId) {
		return Lists.transform(ctrl.getLocations(companyId), LocationProfileDTO.toDTO);
	}
	
	@POST
	@Consumes("application/json")
	@Produces(MediaType.TEXT_PLAIN)
	public String createLocation(JSONObject parameters) {
		long originalLocationId;
		long newOwnerAccountId;
		try {
			originalLocationId = parameters.getLong("copyId");
			newOwnerAccountId = parameters.getLong("ownerAccountId");
		} catch (JSONException e) {
			throw new ValidationException("Invalid JSON or \"copyId\" and \"ownerAccountId\" field not set.");
		}
		
		if(originalLocationId == 0 || newOwnerAccountId == 0) {
			throw new ValidationException("copyId or ownerAccountId parameter not set.");
		}
		QueueFactory.getDefaultQueue().add(
				TaskOptions.Builder
						.withUrl("/" + uriInfo.getPath() + "/processcopy")
						.param("copyId", Long.toString(originalLocationId))
						.param("ownerAccountId", Long.toString(newOwnerAccountId)));
		return "Task queued.";
	}
	
	@POST
	@Path("processcopy")
	public Response processCopyLocation(@FormParam("copyId") long originalLocationId,@FormParam("ownerAccountId") long newOwnerAccountId) {
		locationManagement.copyLocationAndAllEntities(originalLocationId, newOwnerAccountId);
		
		return Response.ok().build();
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
	@Path("{locationId}/channels/{clientId}/warning")
	@Consumes("application/json")
	@Produces("application/json")
	public ChannelDTO sendChannelWarning(@PathParam("locationId") long locationId, @PathParam("clientId") String clientId) {
		return new ChannelDTO(channelController.getChannelAndPostWarningEvent(locationId, clientId));
	}
	
	@DELETE
	@Path("{locationId}/channels/{clientId}")
	public void removeChannel(@PathParam("locationId") long locationId, @PathParam("clientId") String clientId) {
		channelController.removeChannelTracking(locationId, clientId);
	}
	
	@POST
	@Path("{locationId}/offlinewarning")
	@Consumes("application/json")
	@Produces("application/json")
	public void sendOfflineWarning(@PathParam("locationId") long locationId) {
		channelController.sendLocationOfflineWarning(locationId);
	}
	
	@GET
	@Path("reports")
	@Consumes("application/json")
	@Produces("application/json")
	public List<LocationReportDTO> getReport(@QueryParam("fromDate") long fromTimeStamp, @QueryParam("toDate") long toTimeStamp) {
		return reportController.getReportForAllLocationsAndKPIs(new Date(fromTimeStamp), new Date(toTimeStamp));
	}
}
