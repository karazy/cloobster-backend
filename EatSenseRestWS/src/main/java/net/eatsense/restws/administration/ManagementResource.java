package net.eatsense.restws.administration;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.core.ResourceContext;

public class ManagementResource {
	
	@Context
	private ResourceContext resourceContext;
	
	public ManagementResource() {
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
}
