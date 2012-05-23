package net.eatsense.restws;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.eatsense.controller.ImportController;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.util.DummyDataDumper;

@Path("admin/services")
public class AdminResource {
	
	
	private DummyDataDumper ddd;
	private ImportController importCtrl;

	@Inject
	public AdminResource(DummyDataDumper ddd, ImportController importCtr) {
		super();
		this.ddd = ddd;
		this.importCtrl = importCtr;
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
