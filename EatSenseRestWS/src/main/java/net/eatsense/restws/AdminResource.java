package net.eatsense.restws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import net.eatsense.controller.ImportController;
import net.eatsense.controller.TemplateController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.domain.TrashEntry;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.templates.Template;
import net.eatsense.util.DummyDataDumper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

@Path("admin/services")
public class AdminResource {
	private final NicknameAdjectiveRepository adjectiveRepo;
	private final NicknameNounRepository nounRepo;
	private final DummyDataDumper ddd;
	private final ImportController importCtrl;

	private final BusinessRepository businessRepo;
	protected final Logger logger;
	private final boolean devEnvironment;
	private final TemplateController templateCtrl;
	private final AccountRepository accountRepo;

	@Inject
	public AdminResource(ServletContext servletContext, DummyDataDumper ddd,
			ImportController importCtr, BusinessRepository businessRepo,
			NicknameAdjectiveRepository adjRepo, NicknameNounRepository nounRepo, TemplateController templateCtrl, AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
		this.templateCtrl = templateCtrl;
		this.logger =  LoggerFactory.getLogger(this.getClass());
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.adjectiveRepo = adjRepo;
		this.nounRepo = nounRepo;
		this.businessRepo = businessRepo;
		String environment = servletContext.getInitParameter("net.karazy.environment");
		logger.info("net.karazy.environment: {}", environment);
		// Check for dev environment
		devEnvironment = "dev".equals(environment);
	}
	
	@GET
	@Path("trash")
	@Produces("application/json; charset=UTF-8")
	public List<TrashEntry> getAllTrash() {
		return businessRepo.getAllTrash();
	}

	@DELETE
	@Path("trash/{id}")
	@Produces("application/json; charset=UTF-8")
	public void restoreTrash(@PathParam("id") Long trashEntryId, @QueryParam("restore") boolean restore) {
		if(restore == true) {
			// For the moment only business.
			try {
				businessRepo.restoreTrashedEntity(new Key<TrashEntry>(TrashEntry.class, trashEntryId));
			} catch (NotFoundException e) {
				throw new net.eatsense.exceptions.NotFoundException();
			}
		}
	}
	
	@PUT
	@Path("templates/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Template addOrSaveTemplate(@PathParam("id") String id, Template template) {
		return templateCtrl.saveOrUpdateTemplate(template);
	}
	
	@GET
	@Path("templates")
	@Produces("application/json; charset=UTF-8")
	public List<Template> getTemplates() {
		return templateCtrl.getTemplates();
	}
	
	@POST
	@Path("templates")
	@Produces("application/json; charset=UTF-8")
	public List<Template> createTemplates() {
		return templateCtrl.initTemplates("account-confirm-email",
				"newsletter-email-registered", "account-confirmed",
				"account-forgotpassword-email", "account-setup-email",
				"account-notice-password-update",
				"account-confirm-email-update", "account-notice-email-update",
				"customer-account-confirm-email");
	}
	
	@POST
	@Path("nicknames/adjectives")
	@Consumes("application/json; charset=UTF-8")
	public void addNicknameAdjectives(List<NicknameAdjective> adjectives) {
		adjectiveRepo.ofy().put(adjectives);
	}
	
	@POST
	@Path("nicknames/nouns")
	@Consumes("application/json; charset=UTF-8")
	public void addNicknameNouns(List<NicknameNoun> nouns) {
		nounRepo.ofy().put(nouns);
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
	
	/**
	 * Fix for a temporary bug, reload all accounts.
	 */
	@PUT
	@Path("accounts/fixbusinesses")
	public void reloadAccounts() {
		Collection<Account> allAccounts = accountRepo.getAll();
		for (Account account : allAccounts) {
			// Readd Oriental to demo account.
			logger.info("Rewriting account with id: {}", account.getId());
			if(account.getId().longValue() == 12) {
				if(account.getBusinesses() == null) {
					account.setBusinesses(new ArrayList<Key<Business>>());
				}
				 Key<Business> orientalKey = new Key<Business>(Business.class, 10002);
				 logger.info("adding business {} to demo account", orientalKey);
				 if(!account.getBusinesses().contains(orientalKey)) {
					 account.getBusinesses().add(orientalKey);
				 }
			}
			account.getBusinesses();
		}
		accountRepo.saveOrUpdate(allAccounts);
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
	
	@POST
	@Path("businesses/{id}/feedbackforms")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO importNewBusinessFeedback(@PathParam("id") Long businessId, FeedbackFormDTO feedbackFormData ) {
		return importCtrl.importFeedbackForm(businessId, feedbackFormData);
	}
	
	/**
	 * Deletes all data. Use at your own risk.
	 */
	@DELETE
	@Path("datastore/all")
	public void deleteAllData() {
		if(devEnvironment)
			importCtrl.deleteAllData();
		else
			throw new WebApplicationException(405);
	}
	
	/**
	 * Delete all live data (Orders with customer choices, CheckIns, Bills, Requests)
	 */
	@DELETE
	@Path("datastore/live")
	public void deleteLiveData() {
		if(devEnvironment)
			importCtrl.deleteLiveData();
		else
			throw new WebApplicationException(405);
	}
}
