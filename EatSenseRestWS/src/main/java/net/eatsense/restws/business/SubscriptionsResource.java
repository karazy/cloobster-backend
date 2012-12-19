package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import net.eatsense.controller.SubscriptionController;
import net.eatsense.representation.SubscriptionDTO;

@Path("b/subscriptions")
public class SubscriptionsResource {
	private final SubscriptionController ctrl;

	@Inject
	public SubscriptionsResource(SubscriptionController ctrl) {
		super();
		this.ctrl = ctrl;
	}
	
	@GET 
	@Produces("application/json")
	public Iterable<SubscriptionDTO> getSubscriptionTemplates() {
		return Iterables.transform(ctrl.get(true, 0, null), SubscriptionDTO.toDTO);
	}
}
