package net.eatsense.restws;

import java.util.ArrayList;
import java.util.Calendar;
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

import net.eatsense.configuration.Configuration;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.TemplateController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.domain.TrashEntry;
import net.eatsense.domain.embedded.Channel;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.templates.Template;
import net.eatsense.util.DummyDataDumper;
import net.eatsense.util.InfoPageGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
	private final ChannelController channelCtrl;
	private Configuration configuration;
	private FeedbackFormRepository feedbackFormRepo;
	private Provider<InfoPageGenerator> infoPageGen;

	@Inject
	public AdminResource(ServletContext servletContext, DummyDataDumper ddd,
			ImportController importCtr, BusinessRepository businessRepo,
			NicknameAdjectiveRepository adjRepo,
			NicknameNounRepository nounRepo, TemplateController templateCtrl, AccountRepository accountRepo, ChannelController channelCtrl, FeedbackFormRepository feedbackFormRepo, Configuration configuration, Provider<InfoPageGenerator> infoPageGenerator) {
		super();
		this.channelCtrl = channelCtrl;
		this.accountRepo = accountRepo;
		this.templateCtrl = templateCtrl;
		this.logger =  LoggerFactory.getLogger(this.getClass());
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.adjectiveRepo = adjRepo;
		this.nounRepo = nounRepo;
		this.configuration = configuration;
		this.businessRepo = businessRepo;
		this.feedbackFormRepo = feedbackFormRepo;
		this.infoPageGen = infoPageGenerator;
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
	
	@GET
	@Path("configuration/defaultfeedbackform")
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO getDefaultFeedbackForm() {
		Key<FeedbackForm> defaultFeedbackFormKey = configuration.getDefaultFeedbackForm();
		
		if(defaultFeedbackFormKey == null) {
			return new FeedbackFormDTO();			
		}
		
		FeedbackForm defaultFeedbackForm;
		try {
			defaultFeedbackForm = feedbackFormRepo.getByKey(defaultFeedbackFormKey);
		} catch (NotFoundException e) {
			return new FeedbackFormDTO();
		}
			
		return new FeedbackFormDTO(defaultFeedbackForm);
	}
	
	@PUT
	@Path("configuration/defaultfeedbackform")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO importDefaultBusinessFeedback(FeedbackFormDTO feedbackFormData ) {
		return importCtrl.importDefaultFeedbackForm(feedbackFormData);
	}
	
	@POST
	@Path("businesses/{id}/feedbackforms")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO importNewBusinessFeedback(@PathParam("id") Long businessId, FeedbackFormDTO feedbackFormData ) {
		return importCtrl.importFeedbackForm(businessId, feedbackFormData);
	}
	
	@POST
	@Path("channels/messages")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public MessageDTO sendCockpitUpdateMessage(MessageDTO message) {
		
		for(Business business :  businessRepo.query()) {
			channelCtrl.sendMessage(business, message);
		}
		
		return message;
	}
	
	@POST
	@Path("businesses/{businessId}/infopages/{count}")
	@Produces("application/json; charset=UTF-8")
	public List<InfoPageDTO> generateInfoPages(@PathParam("businessId")Long businessId, @PathParam("count") int count) {
		return infoPageGen.get().generate(Business.getKey(businessId), count);
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
