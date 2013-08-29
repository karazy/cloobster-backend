package net.eatsense.restws.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import javax.ws.rs.core.Context;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.SpotPurePDFConfiguration;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.ConfigurationController;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.TemplateController;
import net.eatsense.domain.Business;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.domain.TrashEntry;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.representation.LocationDTO;
import net.eatsense.representation.LocationImportDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.templates.Template;
import net.eatsense.util.DummyDataDumper;
import net.eatsense.util.InfoPageGenerator;
import net.eatsense.util.TestDataGenerator;
import net.eatsense.validation.ValidationHelper;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class ServicesResource {
	private final NicknameAdjectiveRepository adjectiveRepo;
	private final NicknameNounRepository nounRepo;
	private final DummyDataDumper ddd;
	private final ImportController importCtrl;

	private final LocationRepository businessRepo;
	protected final Logger logger;
	private final boolean devEnvironment;
	private final TemplateController templateCtrl;
	private final ChannelController channelCtrl;
	private Configuration configuration;
	private FeedbackFormRepository feedbackFormRepo;
	private Provider<InfoPageGenerator> infoPageGen;
	private final ValidationHelper validator;
	@Context
	private ResourceContext resourceContext;
	private final TestDataGenerator testDataGen;
	private final ConfigurationController configCtrl;

	@Inject
	public ServicesResource(ServletContext servletContext, DummyDataDumper ddd,
			ImportController importCtr, LocationRepository businessRepo,
			NicknameAdjectiveRepository adjRepo,
			NicknameNounRepository nounRepo, TemplateController templateCtrl,
			ChannelController channelCtrl,
			FeedbackFormRepository feedbackFormRepo,
			Configuration configuration,
			Provider<InfoPageGenerator> infoPageGenerator,
			ValidationHelper validator,
			TestDataGenerator testDataGen, ConfigurationController configCtrl) {
		super();
		this.channelCtrl = channelCtrl;
		this.templateCtrl = templateCtrl;
		this.validator = validator;
		this.testDataGen = testDataGen;
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.adjectiveRepo = adjRepo;
		this.nounRepo = nounRepo;
		this.configuration = configuration;
		this.businessRepo = businessRepo;
		this.feedbackFormRepo = feedbackFormRepo;
		this.infoPageGen = infoPageGenerator;
		String environment = servletContext
				.getInitParameter("net.karazy.environment");
		logger.info("net.karazy.environment: {}", environment);
		// Check for dev environment
		devEnvironment = "dev".equals(environment);
		this.configCtrl = configCtrl;
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
		return templateCtrl.initAllTemplate();
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
	
	@GET
	@Path("businesses")
	@Produces("application/json; charset=UTF-8")
	public List<LocationDTO> getBusinesses() {
		List<LocationDTO> businesses = new ArrayList<LocationDTO>();
		for (Business business : businessRepo.getAll()) {
			businesses.add(new LocationDTO(business));
		}
		return businesses;
	}
			
	@POST
	@Path("businesses")
	@Consumes("application/json; charset=UTF-8")
	@Produces("text/plain; charset=UTF-8")
	public String importNewBusiness(LocationImportDTO newBusiness ) {
		Business business =  importCtrl.addBusiness(newBusiness, null);
		
		if(business.getId() == null)
			throw new ServiceException("Error:\n" + importCtrl.getReturnMessage());
		else
		    return String.valueOf(business.getId());
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
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO importDefaultBusinessFeedback(FeedbackFormDTO feedbackFormData ) {
		return importCtrl.importDefaultFeedbackForm(feedbackFormData);
	}
	
	@GET
	@Path("configuration/spotpurepdf")
	public SpotPurePDFConfiguration getSpotPurePDFConfiguration() {
		return configuration.getSpotPurePdfConfiguration();
	}
	
	@PUT
	@Path("configuration/spotpurepdf")
	public SpotPurePDFConfiguration updateSpotPurePDFConfiguration(SpotPurePDFConfiguration spotConfig) {
		validator.validate(spotConfig);
		
		configuration.setSpotPurePdfConfiguration(spotConfig);
		configuration.save();
		
		return configuration.getSpotPurePdfConfiguration();
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
		return infoPageGen.get().generate(Business.getKey(businessId), count , null);
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
	
	@Path("dataupgrades")
	public DataUpgradesResource getDataUpgradesResource() {
		return resourceContext.getResource(DataUpgradesResource.class);
	}
	
	@POST
	@Path("testdata")
	public void createTestData() {
		if(devEnvironment)
			testDataGen.createTestData();
		else
			throw new WebApplicationException(405);
	}
	
	@DELETE
	@Path("testdata")
	public void deleteTestData() {
		if(devEnvironment)
			testDataGen.deleteTestData();
		else
			throw new WebApplicationException(405);
	}
	
	//Whitelabel Methods
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@Path("configuration/whitelabels")
	public List<Map<String, String>> getWhitelabels(){
		return configCtrl.getAllWhitelabels(configuration.getWhitelabels());
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@Path("configuration/whitelabels/{name}")
	public Map<String, String> getWhitelabel(@PathParam("name") String name){
		return configCtrl.getWhitelabel(configuration.getWhitelabels(), name);
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@Path("configuration/whitelabels/{name}")
	public Map<String, String> saveWhitelabel(@PathParam("name") String name, JSONObject configMap){
		return configCtrl.saveWhitelabel(configuration.getWhitelabels(), name, configMap);
	}
	
	@DELETE
	@Path("configuration/whitelabels/{name}")
	public void deleteWhitelabel(@PathParam("name") String name){
		configCtrl.deleteWhitelabel(configuration.getWhitelabels(), name);
	}
	
//	@GET
//	@Path("configuration/whitelabel")
//	public List<WhiteLabelConfiguration> getWhitelabels() {
//		return null;
//	}
//	
//	@PUT
//	@Path("configuration/whitelabel/{wlID}")
//	public WhiteLabelConfiguration updateWhitelabel(@PathParam("wlId") long id, WhiteLabelConfiguration wlc) {
//		return null;
//	}
//	
//	@POST
//	@Path("configuration/whitelabel")
//	public WhiteLabelConfiguration createWhitelabel(WhiteLabelConfiguration wlc) {
//		return null;
//	}
//	
//	@DELETE
//	@Path("configuration/whitelabel/{wlID}")
//	public WhiteLabelConfiguration createWhitelabel(@PathParam("wlId") long id) {
//		return null;
//	}
	
}
