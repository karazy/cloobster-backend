package net.eatsense.restws.administration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.representation.SubscriptionDTO;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class SubscriptionTemplatesResource {
	private final SubscriptionController subCtrl;
	
	@Inject
	public SubscriptionTemplatesResource(SubscriptionController subCtrl) {
		super();
		this.subCtrl = subCtrl;
	}

	@GET
	@Produces("application/json")
	public Iterable<SubscriptionDTO> getSubscriptions(@QueryParam("template") boolean isTemplate, @QueryParam("businessId") long businessId, @QueryParam("status") SubscriptionStatus status) {
		return Iterables.transform(subCtrl.get(isTemplate, businessId, status), SubscriptionDTO.toDTO);
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO createPackage(SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.createAndSaveTemplate(subscriptionData));
	}
	
	@GET
	@Path("{id}")
	@Produces("application/json")
	public SubscriptionDTO getPackage(@PathParam("id") Long id) {
		return SubscriptionDTO.toDTO.apply(subCtrl.getTemplate(id));
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO updatePackage(@PathParam("id") Long id, SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.update(subCtrl.getTemplate(id), subscriptionData));
	}
	
	@DELETE
	@Path("{id}")
	public void deletePackage(@PathParam("id") Long id) {
		subCtrl.deletePackage(id);
	}	
}
