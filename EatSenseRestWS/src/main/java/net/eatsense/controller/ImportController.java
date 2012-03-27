package net.eatsense.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.representation.SpotDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Controller for data import of complete business data.
 * 
 * @author Nils Weiher
 *
 */
public class ImportController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private BusinessRepository businessRepo;
	private SpotRepository spotRepo;
	private MenuRepository menuRepo;
	private ProductRepository productRepo;
	private ChoiceRepository choiceRepo;
	private CheckInRepository checkinRepo;
	private OrderRepository orderRepo;
	private RequestRepository requestRepo;
	
	private String returnMessage;
	
	public String getReturnMessage() {
		return returnMessage;
	}

	@Inject
    private Validator validator;
	private OrderChoiceRepository orderChoiceRepo;
	private BillRepository billRepo;
	

	@Inject
	public ImportController(BusinessRepository businessRepo, SpotRepository sr, MenuRepository mr, ProductRepository pr, ChoiceRepository cr, CheckInRepository chkr, OrderRepository or, OrderChoiceRepository ocr, BillRepository br, RequestRepository reqr) {
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
	}
	
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

	public Long addBusiness(BusinessImportDTO businessData) {
		if (!isValidBusinessData(businessData)) {
			logger.info("Invalid business data, import aborted.");
			logger.info(returnMessage);
			return null;
		}
		
		logger.info("New import request recieved for business: " + businessData.getName() );
		
		Key<Business> kR = createAndSaveBusiness(businessData.getName(), businessData.getDescription(), businessData.getPayments() );
		if(kR == null) {
			logger.info("Creation of business in datastore failed, import aborted.");
			return null;
		}
		
		for(SpotDTO spot : businessData.getSpots()) {
			if( createAndSaveSpot(kR, spot.getName(), spot.getBarcode(), spot.getGroupTag()) == null )
				logger.info("Error while saving spot with name: " + spot.getName());
		}
		
		for(MenuDTO menu : businessData.getMenus()) {
			Key<Menu> kM = createAndSaveMenu(kR, menu.getTitle(), menu.getDescription());
			
			if(kM != null) {
				// Continue with adding products to the menu ...
				for (ProductDTO productData : menu.getProducts()) {
					Product newProduct = createProduct(kM,kR, productData.getName(), productData.getPrice(), productData.getShortDesc(), productData.getLongDesc());
					Key<Product> kP = productRepo.saveOrUpdate(newProduct);
					if(kP != null) {
						if(productData.getChoices() != null) {
							List<Key<Choice>> choices = new ArrayList<Key<Choice>>();
												
							for(ChoiceDTO choiceData : productData.getChoices()) {
								Key<Choice> kC = createAndSaveChoice(kP,kR, choiceData.getText(), choiceData.getPrice(), choiceData.getOverridePrice(),
										choiceData.getMinOccurence(), choiceData.getMaxOccurence(), choiceData.getIncluded(), choiceData.getOptions());
								if(kC == null) {
									logger.info("Error while saving choice with text: " +choiceData.getText());
								}
								else
									choices.add(kC);
							}
						
							newProduct.setChoices(choices);
							productRepo.saveOrUpdate(newProduct);
							logger.info("Saved choices for product (" +kP.getId()+ ")." );
						}
					}
					else {
						logger.info("Error while saving product with name: " +productData.getName());
					}
				}
			}
			else {
				logger.info("Error while saving menu with title: " +menu.getTitle());
			}

		}
		
		logger.info("Import finished for business: " +businessData.getName() );
		return kR.getId();
	}
	
	private Key<Business> createAndSaveBusiness(String name, String desc, Collection<PaymentMethod> paymentMethods) {
		logger.info("Creating new business with data: " + name + ", " + desc );
		
		Business business = new Business();
		business.setName(name);
		business.setDescription(desc);
		business.setPaymentMethods(new ArrayList<PaymentMethod>(paymentMethods));
		
		Key<Business> businessKey = businessRepo.saveOrUpdate(business);
		logger.info("Created new business with id: " + businessKey.getId());
		return businessKey;
	}
	
	private Key<Spot> createAndSaveSpot(Key<Business> businessKey, String name, String barcode, String groupTag) {
		if(businessKey == null)
			throw new NullPointerException("businessKey was not set");
		logger.info("Creating new spot for business ("+ businessKey.getId() + ") with name: " + name );
		
		Spot s = new Spot();
		s.setBusiness(businessKey);
		s.setName(name);
		s.setBarcode(barcode);
		s.setGroupTag(groupTag);
		
		Key<Spot> kS = spotRepo.saveOrUpdate(s);
		logger.info("Created new spot with id: " + kS.getId());
		return kS;
	}
	
	private Key<Menu> createAndSaveMenu (Key<Business> businessKey, String title, String description) {
		if(businessKey == null)
			throw new NullPointerException("businessKey was not set");
		logger.info("Creating new menu for business ("+ businessKey.getId() + ") with title: " + title );
		
		Menu menu = new Menu();
		menu.setTitle(title);
		menu.setBusiness(businessKey);
		menu.setDescription(description);
				
		Key<Menu> kM = menuRepo.saveOrUpdate(menu);
		logger.info("Created new menu with id: " + kM.getId());
		return kM;
	}
	
	private Product createProduct(Key<Menu> menuKey, Key<Business> business, String name, Float price, String shortDesc, String longDesc)	{
		if(menuKey == null)
			throw new NullPointerException("menuKey was not set");
		logger.info("Creating new product for menu ("+ menuKey.getId() + ") with name: " + name );
		
		Product product = new Product();
		product.setMenu(menuKey);
		product.setBusiness(business);
		product.setName(name);
		product.setPrice(price);
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		
		return product;
	}
	
	private Key<Choice> createAndSaveChoice(Key<Product> productKey, Key<Business> businessKey, String text, float price, ChoiceOverridePrice overridePrice, int minOccurence, int maxOccurence, int includedChoices, Collection<ProductOption> availableOptions) {
		if(productKey == null)
			throw new NullPointerException("productKey was not set");
		logger.info("Creating new choice for product ("+ productKey.getId() + ") with text: " +text);
		
		Choice choice = new Choice();
		choice.setProduct(productKey);
		choice.setBusiness(businessKey);
		choice.setText(text);
		choice.setPrice(price);
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
	
	private boolean isValidBusinessData(BusinessImportDTO business) throws IllegalArgumentException,NullPointerException {
		StringBuilder messageBuilder = new StringBuilder();
		boolean isValid = true;
		if( business != null ) {
			Set<ConstraintViolation<BusinessImportDTO>> violation = validator.validate(business);
			
			if(violation.isEmpty()) {
				isValid = true;
			}
			else {
				isValid = false;
				messageBuilder.append("business data not valid:\n");
				for (ConstraintViolation<BusinessImportDTO> constraintViolation : violation) {
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
		orderChoiceRepo.ofy().delete(orderRepo.getAllKeys());
		requestRepo.ofy().delete(requestRepo.getAllKeys());
		billRepo.ofy().delete(billRepo.getAllKeys());
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
		orderChoiceRepo.ofy().delete(orderRepo.getAllKeys());
		billRepo.ofy().delete(billRepo.getAllKeys());
	}
}
