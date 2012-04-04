package net.eatsense.restws.business;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.controller.BillController;
import net.eatsense.representation.BillDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.ResourceContext;

public class BillResource {
	public BillResource(BillController billController) {
		super();
		this.billController = billController;
	}
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private ResourceContext resourceContext;
	
	private BillController billController;
	private Long businessId;
	private Long billId;
	
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public void setBillId(Long billId) {
		this.billId = billId;
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public BillDTO updateBill(BillDTO billData) {
		return billController.updateBill(businessId, billId, billData);
	}
}
