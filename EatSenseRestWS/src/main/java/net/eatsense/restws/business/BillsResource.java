package net.eatsense.restws.business;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.quartz.xml.ValidationException;

import net.eatsense.auth.Role;
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
	
	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public BillDTO getBills(@QueryParam("checkInId") Long checkInId) {
		BillDTO billData = billController.getBillForCheckIn(business, checkInId);
		if(billData == null)
			throw new NotFoundException();
		return billData;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public BillDTO createBill(BillDTO billData) {
		if(billData.getCheckInId() == null) {
			throw new net.eatsense.exceptions.ValidationException("checkInId was null", "validationError");
			
		}
		//TODO finish method
		return null;//billController.createBill(business, checkIn, billData);
	}
	
	
	@Path("{id}")
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public BillDTO updateBill(@PathParam("id") Long billId, BillDTO billData) {
		Bill bill = billController.getBill(business, billId);
		if(bill == null)
			throw new NotFoundException();

		return billController.updateBill(business, bill, billData);
	}

}
