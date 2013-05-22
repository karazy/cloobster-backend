package net.eatsense.restws.customer;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.LocationController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.filter.HttpMethods;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.representation.LocationProfileDTO;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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

	private final LocationController locationCtrl;
	
	@Inject
	public LocationsResource(LocationRepository repo, LocationController locationCtrl) {
		this.businessRepo = repo;
		this.locationCtrl = locationCtrl;
	}
	
	@GET
	@Produces("application/json; charset=utf-8")
	public List<LocationProfileDTO> getBusinesses(@QueryParam("spotCode") String spotCode) {
		
		try {
			return ImmutableList.of(new LocationProfileDTO(locationCtrl.getLocationBySpotCode(spotCode)));
		} catch (net.eatsense.exceptions.NotFoundException e) {
			// Return empty collection if no business found for this code.
			return ImmutableList.of();
		}
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
