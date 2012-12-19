package net.eatsense.restws.business;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.representation.SubscriptionDTO;

public class SubscriptionsResource {
	private final SubscriptionController ctrl;
	private Business business;

	public Business getBusiness() {
		return business;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	@Inject
	public SubscriptionsResource(SubscriptionController ctrl) {
		super();
		this.ctrl = ctrl;
	}
	
	
	@GET
	@Produces("application/json")
	public Iterable<SubscriptionDTO> getSubscriptions() {
		return Iterables.transform(ctrl.get(business.getId()), SubscriptionDTO.toDTO);
	}

	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO createPendingSubscription(SubscriptionDTO subscriptionData) {
		return new SubscriptionDTO(ctrl.createAndSetSubscription(subscriptionData.getTemplateId(), SubscriptionStatus.PENDING, business.getId()));
	}
	
	@GET
	@Path("{subscriptionId}")
	@Produces("application/json")
	public SubscriptionDTO getSubscription(@PathParam("subscriptionId") long subscriptionId) {
		return new SubscriptionDTO(ctrl.get(business.getId(), subscriptionId));
	}
	
	@DELETE
	@Path("{subscriptionId}")
	public void removeSubscription(@PathParam("subscriptionId") long subscriptionId ) {
		ctrl.removePendingSubscription(business, subscriptionId);
	}
}
