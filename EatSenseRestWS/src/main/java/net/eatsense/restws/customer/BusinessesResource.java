package net.eatsense.restws.customer;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.controller.ImportController;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.util.DummyDataDumper;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

/**
 * Provides a restful interface to access businesses. That could be optaining
 * informations to a business, checkIn etc.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Path("c/businesses")
public class BusinessesResource{
	@Context
	private ResourceContext resourceContext;

	private BusinessRepository businessRepo;
	private DummyDataDumper ddd;
	private ImportController importCtrl;

	@Context
	HttpServletRequest servletRequest;
	
	@Inject
	public BusinessesResource(BusinessRepository repo, DummyDataDumper ddd, ImportController importCtr) {
		this.businessRepo = repo;
		this.ddd = ddd;
		this.importCtrl = importCtr;
	}

	/**
	 * Returns a list of all businesses
	 * 
	 * @return all businesses
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<Business> listAll() {
		Collection<Business> list =  businessRepo.getAll();
		return list;
	}
	
	@Path("{businessId}")
	public BusinessResource getBusinessResource(@PathParam("businessId") Long businessId) {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		
		BusinessResource businessResource = resourceContext.getResource(BusinessResource.class);
		businessResource.setBusiness(business);
		businessResource.setCheckIn(checkIn);
		
		return businessResource;
	}
		
	@PUT
	@Path("dummies")
	public void dummyData() {
		ddd.generateDummyBusinesses();
	}
	
	@PUT
	@Path("dummieusers")
	public void dummyUsers() {
		ddd.generateDummyUsers();
	}
		
	@PUT
	@Path("import")
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
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * {@link ImportController#deleteAllData()}
	 * Also recreates the dummy users.
	 */
	@DELETE
	@Path("all")
	public void deleteAllData() {
		//TODO DELETE
		importCtrl.deleteAllData();
	}
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * {@link ImportController#deleteAllData()}
	 */
	@DELETE
	@Path("livedata")
	public void deleteLiveData() {
		//TODO DELETE
		importCtrl.deleteLiveData();
	}

}
