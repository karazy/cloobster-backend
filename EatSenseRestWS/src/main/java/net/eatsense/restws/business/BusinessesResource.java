package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.BusinessController;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

@Path("b/businesses")
public class BusinessesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	BusinessController businessController;
	
	@Context
	private ResourceContext resourceContext;
	
	@Inject
	public BusinessesResource(BusinessController businessController) {
		super();
		this.businessController = businessController;
	}
	
	
	@Path("{businessId}")
	public BusinessResource getBusinessResource(@PathParam("businessId") Long businessId) {
		logger.debug("retrieving businessresource");
		BusinessResource businessResource = resourceContext.getResource(BusinessResource.class); 
		businessResource.setBusinessId(businessId);
		
		return businessResource;
	}
}
