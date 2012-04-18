package net.eatsense.restws.business;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.BusinessController;
import net.eatsense.domain.Business;
import net.eatsense.persistence.BusinessRepository;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

@Path("b/businesses")
public class BusinessesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private BusinessRepository businessRepo;
	
	@Context
	private ResourceContext resourceContext;
	
	@Inject
	public BusinessesResource(BusinessRepository businessRepo) {
		super();
		this.businessRepo = businessRepo;
	}
	
	
	@Path("{businessId}")
	@RolesAllowed({"restaurantadmin"})
	public BusinessResource getBusinessResource(@PathParam("businessId") Long businessId) {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}

		BusinessResource businessResource = resourceContext.getResource(BusinessResource.class); 
		businessResource.setBusiness(business);
		
		return businessResource;
	}
}
