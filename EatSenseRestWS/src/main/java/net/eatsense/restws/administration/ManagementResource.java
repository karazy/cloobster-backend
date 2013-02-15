package net.eatsense.restws.administration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.ChannelController;
import net.eatsense.representation.ChannelDTO;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class ManagementResource {
	
	@Context
	private ResourceContext resourceContext;
	private final ChannelController channelCtrl;
	
	@Inject
	public ManagementResource(ChannelController channelCtrl) {
		this.channelCtrl = channelCtrl;
	}
	
	@Path("subscriptions")
	public SubscriptionTemplatesResource getSubsriptionsResource() {
		return resourceContext.getResource(SubscriptionTemplatesResource.class);
	}
	
	@Path("companies")
	public CompaniesResource getCompaniesResource() {
		return resourceContext.getResource(CompaniesResource.class);
	}
	
	@Path("locations")
	public LocationsResource getLocationsResource() {
		return resourceContext.getResource(LocationsResource.class);
	}
	
	@GET
	@Path("channels")
	@Produces("application/json")
	public Iterable<ChannelDTO> getChannels(@QueryParam("locationId") long locationId) {
		return Iterables.transform(channelCtrl.getActiveChannels(locationId),ChannelDTO.toDTO);
	}
	
	@Path("accounts")
	public AccountsResource getAccountsResource() {
		return resourceContext.getResource(AccountsResource.class);
	}
}
