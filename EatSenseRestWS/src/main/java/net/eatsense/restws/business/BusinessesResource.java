package net.eatsense.restws.business;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.HttpMethods;
import net.eatsense.auth.Role;
import net.eatsense.controller.BusinessController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessProfileDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

@Path("b/businesses")
public class BusinessesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private BusinessRepository businessRepo;
	
	@Context
	private ResourceContext resourceContext;
	
	@Context
	HttpServletRequest servletRequest;

	private final BusinessController businessCtrl;
	
	@Inject
	public BusinessesResource(BusinessRepository businessRepo, BusinessController businessCtrl) {
		super();
		this.businessRepo = businessRepo;
		this.businessCtrl = businessCtrl;
	}
	
	@GET
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Collection<BusinessDTO> getBusinessesForAccount(@QueryParam("account") Long accountId) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		//TODO Don't ignore the accountId and implement methods to view businesses of subordinate accounts.
		return businessCtrl.getBusinessDtosForAccount(account);
	}
	
	@POST
	@RolesAllowed(Role.COMPANYOWNER)
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public BusinessProfileDTO newBusiness(BusinessProfileDTO businessData) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return businessCtrl.createBusinessForAccount(account, businessData);
	}
	
	
	@Path("{businessId}")
	public BusinessResource getBusinessResource(@PathParam("businessId") Long businessId) {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		if(business.isTrash()) {
			if(HttpMethods.WRITE_METHODS.contains(servletRequest.getMethod())) {
				throw new IllegalAccessException("Can not modify trashed resource");
			}
		}

		BusinessResource businessResource = resourceContext.getResource(BusinessResource.class); 
		businessResource.setBusiness(business);
		businessResource.setAccount((Account)servletRequest.getAttribute("net.eatsense.domain.Account"));
		
		return businessResource;
	}
}
