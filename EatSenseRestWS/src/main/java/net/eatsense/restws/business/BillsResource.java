package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.BillController;
import net.eatsense.representation.BillDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class BillsResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private BillController billController;
	private Long businessId;
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	
	@Inject
	public BillsResource(BillController billController) {
		this.billController = billController;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public BillDTO getOrders(@QueryParam("checkInId") Long checkInId) {
		BillDTO billData = billController.getBillForCheckIn(businessId, checkInId);
		if(billData == null)
			throw new NotFoundException();
		return billData;
	}	
}
