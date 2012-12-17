package net.eatsense.restws.administration;

import javax.activation.MimeType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import net.eatsense.controller.SubscriptionController;
import net.eatsense.representation.SubscriptionDTO;

public class SubscriptionPackageResource {
	private final SubscriptionController subCtrl;
	
	@Inject
	public SubscriptionPackageResource(SubscriptionController subCtrl) {
		super();
		this.subCtrl = subCtrl;
	}

	@GET
	@Produces("application/json")
	public Iterable<SubscriptionDTO> getAllPackages(@QueryParam("template") boolean isTemplate) {
		return Iterables.transform(subCtrl.getAll(isTemplate), SubscriptionDTO.toDTO);
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO createPackage(SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.createAndSavePackage(subscriptionData));
	}
	
	@GET
	@Path("{name}")
	@Produces("application/json")
	public SubscriptionDTO getPackage(@PathParam("name") String name) {
		return SubscriptionDTO.toDTO.apply(subCtrl.getPackage(name));
	}
	
	@PUT
	@Path("{name}")
	@Consumes("application/json")
	@Produces("application/json")
	public SubscriptionDTO updatePackage(@PathParam("name") String name, SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.update(subCtrl.getPackage(name), subscriptionData));
	}
}
