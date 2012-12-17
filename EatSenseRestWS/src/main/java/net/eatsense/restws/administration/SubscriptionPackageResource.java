package net.eatsense.restws.administration;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
	public Iterable<SubscriptionDTO> getAllPackages() {
		return Iterables.transform(subCtrl.getAllPackages(), SubscriptionDTO.toDTO);
	}
	
	@POST
	public SubscriptionDTO createPackage(SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.createAndSavePackage(subscriptionData));
	}
	
	@GET
	@Path("{name}")
	public SubscriptionDTO getPackage(@PathParam("name") String name) {
		return SubscriptionDTO.toDTO.apply(subCtrl.getPackage(name));
	}
	
	@PUT
	@Path("{name}")
	public SubscriptionDTO updatePackage(@PathParam("name") String name, SubscriptionDTO subscriptionData) {
		return SubscriptionDTO.toDTO.apply(subCtrl.update(subCtrl.getPackage(name), subscriptionData));
	}
}
