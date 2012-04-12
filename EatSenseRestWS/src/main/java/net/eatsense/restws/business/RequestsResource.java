package net.eatsense.restws.business;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.BusinessController;
import net.eatsense.representation.CustomerRequestDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class RequestsResource {
	@Context
	private ResourceContext resourceContext;
	
	private Long businessId;

	private BusinessController businessCtrl;

	@Inject
	public RequestsResource(BusinessController checkInController) {
		this.businessCtrl = checkInController;
	}
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public CustomerRequestDTO getCustomerRequest(@QueryParam("checkInId") Long checkInId) {
		CustomerRequestDTO requestData = businessCtrl.getCustomerRequestData(businessId, checkInId);
		if( requestData == null)
			throw new NotFoundException("No request for this checkin found");
		else
			return requestData;
	}
	
	@DELETE
	@Path("{requestId}")
	public void deleteCustomerRequest(@PathParam("requestId") Long requestId) {
		businessCtrl.deleteCustomerRequest(businessId, requestId);
	}
}
