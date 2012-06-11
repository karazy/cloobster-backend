package net.eatsense.restws.business;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.BusinessController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessProfileDTO;
import net.eatsense.representation.ImageDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

/**
 * Sub-resource representing {@link Business} entities.
 * @author Nils Weiher
 *
 */
public class BusinessResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Context
	ResourceContext resourceContext;
	@Context
	HttpServletRequest servletRequest;
	
	private Business business;
	private BusinessController businessCtrl;
			
	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@Inject
	public BusinessResource(BusinessController businessCtrl) {
		super();
		this.businessCtrl = businessCtrl;
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	public BusinessDTO getBusiness() {
		if(business == null)
			throw new NotFoundException();
		
		return new BusinessProfileDTO(business);
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.BUSINESSADMIN)
	public BusinessProfileDTO updateBusinessProfile(BusinessProfileDTO businessData) {
		return businessCtrl.updateBusiness(business, businessData);
	}
	
	@POST
	@Path("images/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.BUSINESSADMIN)
	public ImageDTO updateOrCreateImage(@PathParam("id") String imageId, ImageDTO updatedImage) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return businessCtrl.updateBusinessImage(account, business, updatedImage);
	}

	@Path("orders")
	public OrdersResource getOrdersResource() {
		OrdersResource ordersResource = resourceContext.getResource(OrdersResource.class);
		ordersResource.setBusiness(business);
		return ordersResource;
	}
	
	@Path("checkins")
	public CheckInsResource getCheckInsResource() {
		CheckInsResource checkInsResource = resourceContext.getResource(CheckInsResource.class);
		
		checkInsResource.setBusiness(business);
		
		return checkInsResource;
	}
	
	@Path("spots")
	public SpotsResource getSpotsResource() {
		SpotsResource spotsResource = resourceContext.getResource(SpotsResource.class);
		
		spotsResource.setBusiness(business);
		
		return spotsResource;
	}
	
	@Path("bills")
	public BillsResource getBillsResource() {
		BillsResource billsResource = resourceContext.getResource(BillsResource.class);
		
		billsResource.setBusiness(business);
		
		return billsResource;
	}
	
	@Path("requests")
	public RequestsResource getRequestsResource() {
		RequestsResource requestsResource = resourceContext.getResource(RequestsResource.class);
		
		requestsResource.setBusiness(business);
		return requestsResource;
	}
}
