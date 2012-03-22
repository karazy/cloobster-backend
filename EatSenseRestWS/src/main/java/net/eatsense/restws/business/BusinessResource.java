package net.eatsense.restws.business;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

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

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
}
