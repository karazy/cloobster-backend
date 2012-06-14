package net.eatsense.restws;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

import net.eatsense.controller.ImportController;
import net.eatsense.domain.Business;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.restws.business.BusinessResource;
import net.eatsense.util.DummyDataDumper;

@Path("admin/services")
public class AdminResource {
	
	@Context
	private ResourceContext resourceContext;
		
	private DummyDataDumper ddd;
	private ImportController importCtrl;

	private final BusinessRepository businessRepo;

	@Inject
	public AdminResource(DummyDataDumper ddd, ImportController importCtr, BusinessRepository businessRepo) {
		super();
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.businessRepo = businessRepo;
	}
	
	@Path("nicknames")
	public NicknameResource getNicknamesResource() {
		return resourceContext.getResource(NicknameResource.class);
	}
	
	@POST
	@Path("accounts/dummies")
	public void dummyUsers() {
		ddd.generateDummyUsers();
	}
	
	@POST
	@Path("businesses/dummies")
	public void dummyData() {
		ddd.generateDummyBusinesses();
	}
	
	@GET
	@Path("businesses")
	@Produces("application/json; charset=UTF-8")
	public List<BusinessDTO> getBusinesses() {
		List<BusinessDTO> businesses = new ArrayList<BusinessDTO>();
		for (Business business : businessRepo.getAll()) {
			businesses.add(new BusinessDTO(business));
		}
		return businesses;
	}
			
	@POST
	@Path("businesses")
	@Consumes("application/json; charset=UTF-8")
	@Produces("text/plain; charset=UTF-8")
	public String importNewBusiness(BusinessImportDTO newBusiness ) {
		Long id =  importCtrl.addBusiness(newBusiness);
		
		if(id == null)
			return "Error:\n" + importCtrl.getReturnMessage();
		else
		    return id.toString();
	}
	
	/**
	 * Deletes all data. Use at your own risk.
	 */
	@DELETE
	@Path("datastore/all")
	public void deleteAllData() {
		importCtrl.deleteAllData();
	}
	
	/**
	 * Delete all live data (Orders with customer choices, CheckIns, Bills, Requests)
	 */
	@DELETE
	@Path("datastore/live")
	public void deleteLiveData() {
		importCtrl.deleteLiveData();
	}
}
