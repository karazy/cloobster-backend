package net.eatsense.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.ChoiceOverridePrice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.PaymentMethod;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.RestaurantDTO;
import net.eatsense.representation.SpotDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Controller for data import of complete restaurant data.
 * 
 * @author Nils Weiher
 *
 */
public class ImportController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private RestaurantRepository restaurantRepo;
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
	public ImportController(RestaurantRepository r, SpotRepository sr, MenuRepository mr, ProductRepository pr, ChoiceRepository cr, CheckInRepository chkr, OrderRepository or, OrderChoiceRepository ocr, BillRepository br, RequestRepository reqr) {
		this.restaurantRepo = r;
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

	public Long addRestaurant(RestaurantDTO restaurantData) {
		if (!isValidRestaurantData(restaurantData)) {
			logger.info("Invalid restaurant data, import aborted.");
			logger.info(returnMessage);
			return null;
		}
		
		logger.info("New import request recieved for restaurant: " + restaurantData.getName() );
		
		Key<Restaurant> kR = createAndSaveRestaurant(restaurantData.getName(), restaurantData.getDescription(), restaurantData.getPayments() );
		if(kR == null) {
			logger.info("Creation of restaurant in datastore failed, import aborted.");
			return null;
		}
		
		for(SpotDTO spot : restaurantData.getSpots()) {
			if( createAndSaveSpot(kR, spot.getName(), spot.getBarcode(), spot.getGroupTag()) == null )
				logger.info("Error while saving spot with name: " + spot.getName());
		}
		
		for(MenuDTO menu : restaurantData.getMenus()) {
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
		
		logger.info("Import finished for restaurant: " +restaurantData.getName() );
		return kR.getId();
	}
	
	private Key<Restaurant> createAndSaveRestaurant(String name, String desc, Collection<PaymentMethod> paymentMethods) {
		logger.info("Creating new restaurant with data: " + name + ", " + desc );
		
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setDescription(desc);
		r.setPaymentMethods(new ArrayList<PaymentMethod>(paymentMethods));
		
		Key<Restaurant> kR = restaurantRepo.saveOrUpdate(r);
		logger.info("Created new restaurant with id: " + kR.getId());
		return kR;
	}
	
	private Key<Spot> createAndSaveSpot(Key<Restaurant> restaurantKey, String name, String barcode, String groupTag) {
		if(restaurantKey == null)
			throw new NullPointerException("restaurantKey was not set");
		logger.info("Creating new spot for restaurant ("+ restaurantKey.getId() + ") with name: " + name );
		
		Spot s = new Spot();
		s.setRestaurant(restaurantKey);
		s.setName(name);
		s.setBarcode(barcode);
		s.setGroupTag(groupTag);
		
		Key<Spot> kS = spotRepo.saveOrUpdate(s);
		logger.info("Created new spot with id: " + kS.getId());
		return kS;
	}
	
	private Key<Menu> createAndSaveMenu (Key<Restaurant> restaurantKey, String title, String description) {
		if(restaurantKey == null)
			throw new NullPointerException("restaurantKey was not set");
		logger.info("Creating new menu for restaurant ("+ restaurantKey.getId() + ") with title: " + title );
		
		Menu menu = new Menu();
		menu.setTitle(title);
		menu.setRestaurant(restaurantKey);
		menu.setDescription(description);
				
		Key<Menu> kM = menuRepo.saveOrUpdate(menu);
		logger.info("Created new menu with id: " + kM.getId());
		return kM;
	}
	
	private Product createProduct(Key<Menu> menuKey, Key<Restaurant> restaurant, String name, Float price, String shortDesc, String longDesc)	{
		if(menuKey == null)
			throw new NullPointerException("menuKey was not set");
		logger.info("Creating new product for menu ("+ menuKey.getId() + ") with name: " + name );
		
		Product product = new Product();
		product.setMenu(menuKey);
		product.setRestaurant(restaurant);
		product.setName(name);
		product.setPrice(price);
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		
		return product;
	}
	
	private Key<Choice> createAndSaveChoice(Key<Product> productKey, Key<Restaurant> restaurantKey, String text, float price, ChoiceOverridePrice overridePrice, int minOccurence, int maxOccurence, int includedChoices, Collection<ProductOption> availableOptions) {
		if(productKey == null)
			throw new NullPointerException("productKey was not set");
		logger.info("Creating new choice for product ("+ productKey.getId() + ") with text: " +text);
		
		Choice choice = new Choice();
		choice.setProduct(productKey);
		choice.setRestaurant(restaurantKey);
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
	
	private boolean isValidRestaurantData(RestaurantDTO restaurant) throws IllegalArgumentException,NullPointerException {
		StringBuilder messageBuilder = new StringBuilder();
		boolean isValid = true;
		if( restaurant != null ) {
			Set<ConstraintViolation<RestaurantDTO>> violation = validator.validate(restaurant);
			
			if(violation.isEmpty()) {
				isValid = true;
			}
			else {
				isValid = false;
				messageBuilder.append("restaurant data not valid:\n");
				for (ConstraintViolation<RestaurantDTO> constraintViolation : violation) {
					messageBuilder.append("\n").append(constraintViolation.getPropertyPath()).append(" ").append(constraintViolation.getMessage());
				}
			}
		}
		else {
			returnMessage = "restaurant object was null";
			return false;
		}
		returnMessage = messageBuilder.toString();
		return isValid;
	}
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * Deletes all data from datastore except nicknames.
	 * Deletes restaurants, spots, checkins, menus, products, choices and so on.
	 */
	public void deleteAllData() {
		restaurantRepo.ofy().delete(restaurantRepo.getAllKeys());
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
	 * Does not delete restaurant data.
	 */
	public void deleteLiveData() {
		checkinRepo.ofy().delete(checkinRepo.ofy().query(CheckIn.class).fetchKeys());
		requestRepo.ofy().delete(requestRepo.getAllKeys());
		orderRepo.ofy().delete(orderRepo.getAllKeys());
		orderChoiceRepo.ofy().delete(orderRepo.getAllKeys());
		billRepo.ofy().delete(billRepo.getAllKeys());
	}
}
