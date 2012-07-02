package net.eatsense.restws.customer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.BusinessRepository;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

/**
 * Provides a restful interface to access businesses. That could be optaining
 * informations to a business, checkIn etc.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Path("c/businesses")
public class BusinessesResource{
	@Context
	private ResourceContext resourceContext;

	private BusinessRepository businessRepo;

	@Context
	HttpServletRequest servletRequest;
	
	@Inject
	public BusinessesResource(BusinessRepository repo) {
		this.businessRepo = repo;
	}

	@Path("{businessId}")
	public BusinessResource getBusinessResource(@PathParam("businessId") Long businessId) {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		
		BusinessResource businessResource = resourceContext.getResource(BusinessResource.class);
		businessResource.setBusiness(business);
		businessResource.setCheckIn(checkIn);
		
		return businessResource;
	}
}
