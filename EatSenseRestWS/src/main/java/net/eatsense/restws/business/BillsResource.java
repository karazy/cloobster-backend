package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.BillController;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.representation.BillDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class BillsResource {
	@Context
	private ResourceContext resourceContext;
	
	private BillController billController;
	private Business business;
		
	@Inject
	public BillsResource(BillController billController) {
		this.billController = billController;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public BillDTO getBills(@QueryParam("checkInId") Long checkInId) {
		BillDTO billData = billController.getBillForCheckIn(business, checkInId);
		if(billData == null)
			throw new NotFoundException();
		return billData;
	}
	
	@Path("{id}")
	public BillResource getBillResource(@PathParam("id") Long billId) {
		Bill bill = billController.getBill(business, billId);
		if(bill == null)
			throw new NotFoundException();
		
		BillResource billResource = resourceContext.getResource(BillResource.class);
		billResource.setBusiness(business);
		billResource.setBill(bill);
		return billResource;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
}
