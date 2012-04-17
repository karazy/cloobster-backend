package net.eatsense.restws.business;

import java.util.ArrayList;
import java.util.Collection;

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
	public Collection<CustomerRequestDTO> getCustomerRequest(@QueryParam("checkInId") Long checkInId, @QueryParam("spotId") Long spotId) {
		return businessCtrl.getCustomerRequestData(businessId, checkInId, spotId);
	}
	
	@DELETE
	@Path("{requestId}")
	public void deleteCustomerRequest(@PathParam("requestId") Long requestId) {
		businessCtrl.deleteCustomerRequest(businessId, requestId);
	}
}
