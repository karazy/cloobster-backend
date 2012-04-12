package net.eatsense.restws.business;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import net.eatsense.persistence.RequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.ResourceContext;

public class BusinessResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Context
	ResourceContext resourceContext;
	
	Long businessId;
	
	@Path("orders")
	public OrdersResource getOrdersResource() {
		OrdersResource ordersResource = resourceContext.getResource(OrdersResource.class);
		ordersResource.setBusinessId(businessId);
		logger.debug("retrieving ordersresource");
		return ordersResource;
	}
	
	@Path("checkins")
	public CheckInsResource getCheckInsResource() {
		CheckInsResource checkInsResource = resourceContext.getResource(CheckInsResource.class);
		
		checkInsResource.setBusinessId(businessId);
		
		return checkInsResource;
	}
	
	@Path("spots")
	public SpotsResource getSpotsResource() {
		SpotsResource spotsResource = resourceContext.getResource(SpotsResource.class);
		
		spotsResource.setBusinessId(businessId);
		
		return spotsResource;
	}
	
	@Path("bills")
	public BillsResource getBillsResource() {
		BillsResource billsResource = resourceContext.getResource(BillsResource.class);
		
		billsResource.setBusinessId(businessId);
		
		return billsResource;
	}
	
	@Path("requests")
	public RequestsResource getRequestsResource() {
		RequestsResource requestsResource = resourceContext.getResource(RequestsResource.class);
		
		requestsResource.setBusinessId(businessId);
		return requestsResource;
	}
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
}
