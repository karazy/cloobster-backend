package net.eatsense.controller;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import net.eatsense.configuration.Configuration;
import net.eatsense.domain.Area;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Company;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.Business;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.FeedbackQuestion;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.AreaImportDTO;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.representation.LocationImportDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.validation.ImportChecks;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;

/**
 * Controller for data import of complete business data.
 * 
 * @author Nils Weiher
 *
 */
public class ImportController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private LocationRepository businessRepo;
	private SpotRepository spotRepo;
	private MenuRepository menuRepo;
	private ProductRepository productRepo;
	private ChoiceRepository choiceRepo;
	private CheckInRepository checkinRepo;
	private OrderRepository orderRepo;
	private RequestRepository requestRepo;
	private AccountRepository accountRepo;
	
	private String returnMessage;
	
	public String getReturnMessage() {
		return returnMessage;
	}

	@Inject
    private Validator validator;
	private OrderChoiceRepository orderChoiceRepo;
	private BillRepository billRepo;
	private FeedbackFormRepository feedbackFormRepo;
	private AreaRepository areaRepo;
	private Provider<Configuration> configProvider;
	private LocationController locationController;
	
	private List<Product> importedProducts = new ArrayList<Product>();
	private List<Area> areas = new ArrayList<Area>();
	private Map<String, Menu> menuMap = new HashMap<String, Menu>();
	private List<Spot> spots = new ArrayList<Spot>();
	

	public List<Spot> getSpots() {
		return spots;
	}

	public List<Product> getImportedProducts() {
		return importedProducts;
	}

	@Inject
	public ImportController(LocationRepository businessRepo, SpotRepository sr,
			MenuRepository mr, ProductRepository pr, ChoiceRepository cr,
			CheckInRepository chkr, OrderRepository or,
			OrderChoiceRepository ocr, BillRepository br,
			RequestRepository reqr, AccountRepository acr,
			FeedbackFormRepository feedbackFormRepo, AreaRepository areaRepo, Provider<Configuration> configProvider, LocationController locationController) {
		this.areaRepo = areaRepo;
		this.businessRepo = businessRepo;
		this.spotRepo = sr;
		this.menuRepo = mr;
		this.productRepo = pr;
		this.choiceRepo = cr;
		this.checkinRepo = chkr;
		this.orderRepo = or;
		this.billRepo = br;
		this.orderChoiceRepo = ocr;
		this.requestRepo = reqr;
		this.accountRepo = acr;
		this.feedbackFormRepo = feedbackFormRepo;
		this.configProvider = configProvider;
		this.locationController = locationController;
	}

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
    
    /**
     * Creates a new {@link FeedbackForm} entity based on the given data
     * and adds it to a {@link Business} identified by the given id.
     * 
     * @param businessId - Id of the business to which the form will be added.
     * @param feedbackFormData - Data for the new {@link FeedbackForm} entity.
     * @return {@link FeedbackFormDTO} - Data object representing the new FeedbackForm.
     */
    public FeedbackFormDTO importFeedbackForm(Long businessId, FeedbackFormDTO feedbackFormData) {
    	checkNotNull(businessId, "business id was null");
    	checkNotNull(feedbackFormData, "feedbackFormData was null");
    	checkNotNull(feedbackFormData.getQuestions(), "questions list was null");
    	
    	Business business = businessRepo.getById(businessId);
    	
    	FeedbackForm feedbackForm = new FeedbackForm();
    	feedbackForm.setDescription(feedbackFormData.getDescription());
    	
    	Long index = 1l;
    	for (FeedbackQuestion question : feedbackFormData.getQuestions()) {
			question.setId(index);
			index++;
		}
    	feedbackForm.setQuestions(feedbackFormData.getQuestions());
    	feedbackForm.setTitle(feedbackFormData.getTitle());
    	
    	Key<FeedbackForm> formKey = feedbackFormRepo.saveOrUpdate(feedbackForm);
    	business.setFeedbackForm(formKey);
    	businessRepo.saveOrUpdate(business);
    	    	
    	return new FeedbackFormDTO(feedbackForm);
    }
    
    /**
     * Update or create the default {@link FeedbackForm} entity based on the given data. 
     * 
     * @param feedbackFormData - Data for the new {@link FeedbackForm} entity.
     * @return {@link FeedbackFormDTO} - Data object representing the new FeedbackForm.
     */
	public FeedbackFormDTO importDefaultFeedbackForm(
			FeedbackFormDTO feedbackFormData) {
		FeedbackForm feedbackForm = new FeedbackForm();
		Configuration config = configProvider.get();
		if(config.getDefaultFeedbackForm() != null) {
			feedbackForm.setId(config.getDefaultFeedbackForm().getId());
		}
    	feedbackForm.setDescription(feedbackFormData.getDescription());
    	
    	Long index = 1l;
    	for (FeedbackQuestion question : feedbackFormData.getQuestions()) {
			question.setId(index);
			index++;
		}
    	feedbackForm.setQuestions(feedbackFormData.getQuestions());
    	feedbackForm.setTitle(feedbackFormData.getTitle());
    	
    	Key<FeedbackForm> formKey = feedbackFormRepo.saveOrUpdate(feedbackForm);
    	
    	if(!Objects.equal(formKey, config.getDefaultFeedbackForm())) {
    		config.setDefaultFeedbackForm(formKey);
    		config.save();
    	}
    	    	
		return new FeedbackFormDTO(feedbackForm);
	}

	public Business addBusiness(LocationImportDTO businessData, Key<Company> companyKey) {
		if (!isValidBusinessData(businessData)) {
			logger.error("Invalid business data, import aborted.");
			logger.error(returnMessage);
			throw new ValidationException(returnMessage);
		}
		
		// Clear all in memory collections of imported entities
		importedProducts.clear();
		menuMap.clear();
		spots.clear();
		
		logger.info("New import request recieved for business: " + businessData.getName() );
		Business business = createAndSaveBusiness(businessData.getName(), businessData.getDescription(), businessData.getAddress(), businessData.getCity(), businessData.getPostcode(), businessData.getPayments(), companyKey );
		Key<Business> kR = business.getKey();
		
		if(kR == null) {
			logger.info("Creation of business in datastore failed, import aborted.");
			return null;
		}
		
		// Create welcome area and spot
		locationController.createWelcomeAreaAndSpot(kR, Optional.fromNullable(Strings.emptyToNull(businessData.getWelcomeBarcode())));
		
		CurrencyUnit currencyUnit = CurrencyUnit.of(businessData.getCurrency());
		
		for(MenuDTO menu : businessData.getMenus()) {
			Key<Menu> kM = createAndSaveMenu(kR, menu.getTitle(), menu.getDescription(), menu.getOrder());
			
			if(kM != null) {
				// Continue with adding products to the menu ...
				for (ProductDTO productData : menu.getProducts()) {
					
					Product newProduct = createProduct(kM,kR, productData.getName(), Money.ofMinor(currencyUnit, productData.getPriceMinor() ), productData.getShortDesc(), productData.getLongDesc(), productData.getOrder(), productData.isSpecial());
					Key<Product> kP = productRepo.saveOrUpdate(newProduct);
					if(kP != null) {
						if(productData.getChoices() != null) {
							List<Key<Choice>> choices = new ArrayList<Key<Choice>>();
							Map<String, List<ChoiceDTO>> groupMap = new HashMap<String, List<ChoiceDTO>>();
							Map<String, ChoiceDTO> parentMap = new HashMap<String, ChoiceDTO>();
							
							for(ChoiceDTO choiceData : productData.getChoices()) {
								// no group name set, just save the choice
								Key<Choice> choiceKey = createAndSaveChoice(kP,kR, choiceData.getText(), Money.ofMinor(currencyUnit, choiceData.getPriceMinor()), choiceData.getOverridePrice(),
											choiceData.getMinOccurence(), choiceData.getMaxOccurence(), choiceData.getIncluded(), choiceData.getOptions());
								choices.add(choiceKey);
							}
						
							newProduct.setChoices(choices);
							productRepo.saveOrUpdate(newProduct);
							logger.info("Saved choices for product (" +kP.getId()+ ")." );
						}
					}
					else {
						logger.error("Error while saving product with name: " +productData.getName());
					}
				}
			}
			else {
				logger.error("Error while saving menu with title: " +menu.getTitle());
			}

		}
		
		// Create service area and spots.
		for(AreaImportDTO area : businessData.getAreas()) {
			ArrayList<Key<Menu>> menuKeys = new ArrayList<Key<Menu>>();
			for (String menuTitle : area.getMenus()) {
				menuKeys.add(menuMap.get(menuTitle).getKey());
			}
			
			Key<Area> kA = createAndSaveArea(kR, area.getName(),
					area.getDescription(), menuKeys, area.isBarcodeRequired(),
					Optional.fromNullable(Strings.emptyToNull(area.getMasterBarcode())));
			
			for(SpotDTO spot : area.getSpots()) {
				if( createAndSaveSpot(kR, spot.getName(), spot.getBarcode(), kA) == null )
					logger.info("Error while saving spot with name: " + spot.getName());
			}
		}
		
		logger.info("Import finished for business: " +businessData.getName() );
		return business;
	}
	
	private Business createAndSaveBusiness(String name, String desc, String address, String city, String postcode, Collection<PaymentMethod> paymentMethods, Key<Company> companyKey) {
		logger.info("Creating new business with data: " + name + ", " + desc );
		
		Business business = new Business();
		business.setName(name);
		business.setDescription(desc);
		business.setAddress(address);
		business.setCity(city);
		business.setPostcode(postcode);
		business.setCompany(companyKey);
		business.setPaymentMethods(new ArrayList<PaymentMethod>(paymentMethods));
		
		Key<Business> businessKey = businessRepo.saveOrUpdate(business);
		logger.info("Created new business: {}", businessKey);
		return business;
	}
	
	private Key<Area> createAndSaveArea(Key<Business> businessKey, String name, String description, List<Key<Menu>> menuKeys, boolean barcodeRequired, Optional<String> optBarcode) {
		checkNotNull(businessKey, "businessKey was null");
		Area area = new Area();
		area.setBusiness(businessKey);
		area.setDescription(description);
		area.setName(name);
		area.setActive(true);
		area.setMenus(menuKeys);
		area.setBarcodeRequired(barcodeRequired);
		Key<Area> kA = areaRepo.saveOrUpdate(area);
		
		logger.info("Created new area with id: " + kA.getId());
		// create Master Spot for Area
		locationController.createMasterSpot(businessKey, kA, optBarcode);
		return kA;
	}
	
	private Key<Spot> createAndSaveSpot(Key<Business> businessKey, String name, String barcode, Key<Area> areaKey) {
		if(businessKey == null)
			throw new NullPointerException("businessKey was not set");
		logger.info("Creating new spot for business ("+ businessKey.getId() + ") with name: " + name );
		
		Spot s = new Spot();
		s.setBusiness(businessKey);
		s.setName(name);
		s.setBarcode(barcode);
		s.setArea(areaKey);
		spots .add(s);
		Key<Spot> kS = spotRepo.saveOrUpdate(s);
		logger.info("Created new spot with id: " + kS.getId());
		return kS;
	}
	
	private Key<Menu> createAndSaveMenu (Key<Business> businessKey, String title, String description, Integer order) {
		if(businessKey == null)
			throw new NullPointerException("businessKey was not set");
		logger.info("Creating new menu for business ("+ businessKey.getId() + ") with title: " + title );
		
		Menu menu = new Menu();
		menu.setActive(true);
		menu.setTitle(title);
		menu.setBusiness(businessKey);
		menu.setDescription(description);
		menu.setOrder(order);
		
		menuMap.put(title, menu);
				
		Key<Menu> kM = menuRepo.saveOrUpdate(menu);
		logger.info("Created new menu with id: " + kM.getId());
		return kM;
	}
	
	private Product createProduct(Key<Menu> menuKey, Key<Business> business, String name, Money price, String shortDesc, String longDesc, Integer order, boolean special)	{
		if(menuKey == null)
			throw new NullPointerException("menuKey was not set");
		logger.info("Creating new product for menu ("+ menuKey.getId() + ") with name: " + name );
		
		Product product = new Product();
		product.setActive(true);
		product.setMenu(menuKey);
		product.setBusiness(business);
		product.setName(name);
		product.setPrice(price.getAmountMinorInt());
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		product.setOrder(order);
		product.setSpecial(special);
		
		importedProducts.add(product);
		
		return product;
	}
	
	private Key<Choice> createAndSaveChoice(Key<Product> productKey,
			Key<Business> businessKey, String text, Money price,
			ChoiceOverridePrice overridePrice, int minOccurence,
			int maxOccurence, int includedChoices,
			Collection<ProductOption> availableOptions) {
		if(productKey == null)
			throw new NullPointerException("productKey was not set");
		logger.info("Creating new choice for product ("+ productKey.getId() + ") with text: " +text);
		
		Choice choice = new Choice();
		choice.setProduct(productKey);
		choice.setBusiness(businessKey);
		choice.setText(text);
		choice.setPrice(price.getAmountMinorLong());
		choice.setOverridePrice(overridePrice);
		choice.setMinOccurence(minOccurence);
		choice.setMaxOccurence(maxOccurence);
		choice.setIncludedChoices(includedChoices);
	
		if(availableOptions != null)
			choice.setOptions(new ArrayList<ProductOption>(availableOptions));
	
		Key<Choice> kC = choiceRepo.saveOrUpdate(choice);
		logger.info("Created choice with id: "+kC.getId());
		return kC;
	}
	
	private boolean isValidBusinessData(LocationImportDTO business) throws IllegalArgumentException,NullPointerException {
		StringBuilder messageBuilder = new StringBuilder();
		boolean isValid = true;
		if( business != null ) {
			Set<ConstraintViolation<LocationImportDTO>> violation = validator.validate(business,Default.class, ImportChecks.class);
			
			if(violation.isEmpty()) {
				isValid = true;
			}
			else {
				isValid = false;
				messageBuilder.append("business data not valid:\n");
				for (ConstraintViolation<LocationImportDTO> constraintViolation : violation) {
					messageBuilder.append("\n").append(constraintViolation.getPropertyPath()).append(" ").append(constraintViolation.getMessage());
				}
			}
		}
		else {
			returnMessage = "business object was null";
			return false;
		}
		returnMessage = messageBuilder.toString();
		return isValid;
	}
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * Deletes all data from datastore except nicknames.
	 * Deletes businesses, spots, checkins, menus, products, choices and so on.
	 */
	public void deleteAllData() {
		businessRepo.ofy().delete(businessRepo.getAllKeys());
		spotRepo.ofy().delete(spotRepo.getAllKeys());
		menuRepo.ofy().delete(menuRepo.getAllKeys());
		productRepo.ofy().delete(productRepo.getAllKeys());
		choiceRepo.ofy().delete(choiceRepo.getAllKeys());
		checkinRepo.ofy().delete(checkinRepo.getAllKeys());
		orderRepo.ofy().delete(orderRepo.getAllKeys());
		orderChoiceRepo.ofy().delete(orderChoiceRepo.getAllKeys());
		requestRepo.ofy().delete(requestRepo.getAllKeys());
		billRepo.ofy().delete(billRepo.getAllKeys());
		accountRepo.ofy().delete(accountRepo.getAllKeys());
	};
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * Deletes only live data like checkIns, requests and so on.
	 * Does not delete business data.
	 */
	public void deleteLiveData() {
		checkinRepo.ofy().delete(checkinRepo.ofy().query(CheckIn.class).fetchKeys());
		requestRepo.ofy().delete(requestRepo.getAllKeys());
		orderRepo.ofy().delete(orderRepo.getAllKeys());
		orderChoiceRepo.ofy().delete(orderChoiceRepo.getAllKeys());
		billRepo.ofy().delete(billRepo.getAllKeys());
	}

}
