package net.eatsense.restws.customer;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.filter.HttpMethods;
import net.eatsense.persistence.LocationRepository;

import com.google.appengine.labs.repackaged.com.google.common.collect.Sets;
import com.google.common.collect.Collections2;
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
public class LocationsResource{
	@Context
	private ResourceContext resourceContext;

	private LocationRepository businessRepo;

	@Context
	HttpServletRequest servletRequest;
	
	@Inject
	public LocationsResource(LocationRepository repo) {
		this.businessRepo = repo;
	}

	@Path("{businessId}")
	public LocationResource getBusinessResource(@PathParam("businessId") Long businessId) {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}
		
		if(business.isTrash()) {
			if(HttpMethods.WRITE_METHODS.contains(servletRequest.getMethod())) {
				throw new IllegalAccessException("Can not modified trashed resource.");
			}
		}
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		
		LocationResource businessResource = resourceContext.getResource(LocationResource.class);
		
		
		businessResource.setAccount(account);
		businessResource.setBusiness(business);
		businessResource.setCheckIn(checkIn);
		
		return businessResource;
	}
}
