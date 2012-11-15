package net.eatsense.restws.business;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.BillController;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.representation.BillDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class BillResource {
	
	@Inject
	public BillResource(BillController billController) {
		super();
		this.billController = billController;
	}
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private BillController billController;
	private Business business;

	private Bill bill;

	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public BillDTO updateBill(BillDTO billData) {
		return billController.updateBill(business, bill, billData);
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}
}
