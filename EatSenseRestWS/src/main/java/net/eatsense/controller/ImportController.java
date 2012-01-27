package net.eatsense.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Validator;
import javax.validation.groups.Default;

import net.eatsense.domain.Choice;
import net.eatsense.domain.ChoiceOverridePrice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
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
	
	@Inject
    private Validator validator;

	@Inject
	public ImportController(RestaurantRepository r, SpotRepository sr, MenuRepository mr, ProductRepository pr, ChoiceRepository cr) {
		this.restaurantRepo = r;
		this.spotRepo = sr;
		this.menuRepo = mr;
		this.productRepo = pr;
		this.choiceRepo = cr;
	}
	
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

	public Long addRestaurant(RestaurantDTO restaurantData) {
		
		logger.info("New import request recieved for restaurant: " + restaurantData.getName() );
		
		Key<Restaurant> kR = createAndSaveRestaurant(restaurantData.getName(), restaurantData.getDescription() );
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
					Product newProduct = createProduct(kM, productData.getName(), productData.getPrice(), productData.getShortDesc(), productData.getLongDesc());
					Key<Product> kP = productRepo.saveOrUpdate(newProduct);
					if(kP != null) {
						if(productData.getChoices() != null) {
							List<Key<Choice>> choices = new ArrayList<Key<Choice>>();
												
							for(ChoiceDTO choiceData : productData.getChoices()) {
								Key<Choice> kC = createAndSaveChoice(kP, choiceData.getText(), choiceData.getPrice(), choiceData.getOverridePrice(),
										choiceData.getMinOccurence(), choiceData.getMaxOccurence(), choiceData.getIncluded(), choiceData.getOptions(), null);
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
	
	private Key<Restaurant> createAndSaveRestaurant(String name, String desc) {
		logger.info("Creating new restaurant with data: " + name + ", " + desc );
		
		Restaurant r = new Restaurant();
		r.setName(name);
		r.setDescription(desc);
		
		validator.validate(r, Default.class);
		
		Key<Restaurant> kR = restaurantRepo.saveOrUpdate(r);
		logger.info("Created new restaurant with id: " + kR.getId());
		return kR;
	}
	
	private Key<Spot> createAndSaveSpot(Key<Restaurant> restaurantKey, String name, String barcode, String groupTag) {
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
		logger.info("Creating new menu for restaurant ("+ restaurantKey.getId() + ") with title: " + title );
		
		Menu menu = new Menu();
		menu.setTitle(title);
		menu.setRestaurant(restaurantKey);
		menu.setDescription(description);
				
		Key<Menu> kM = menuRepo.saveOrUpdate(menu);
		logger.info("Created new menu with id: " + kM.getId());
		return kM;
	}
	
	private Product createProduct(Key<Menu> menuKey, String name, Float price, String shortDesc, String longDesc)	{
		logger.info("Creating new product for menu ("+ menuKey.getId() + ") with name: " + name );
		
		Product product = new Product();
		product.setMenu(menuKey);
		product.setName(name);
		product.setPrice(price);
		product.setShortDesc(shortDesc);
		product.setLongDesc(longDesc);
		
		return product;
	}
	
	private Key<Choice> createAndSaveChoice(Key<Product> productKey, String text, float price, ChoiceOverridePrice overridePrice, int minOccurence, int maxOccurence, int includedChoices, Collection<ProductOption> availableOptions, Collection<Key<Product>> availableProducts) {
		logger.info("Creating new choice for product ("+ productKey.getId() + ") with text: " +text);
		
		Choice choice = new Choice();
		choice.setProduct(productKey);
		choice.setText(text);
		choice.setPrice(price);
		choice.setOverridePrice(overridePrice);
		choice.setMinOccurence(minOccurence);
		choice.setMaxOccurence(maxOccurence);
		choice.setIncludedChoices(includedChoices);
	
		if(availableOptions != null)
			choice.setAvailableChoices(new ArrayList<ProductOption>(availableOptions));
		else if(availableProducts != null)
			choice.setAvailableProducts(new ArrayList<Key<Product>>(availableProducts));
		
		
		Key<Choice> kC = choiceRepo.saveOrUpdate(choice);
		logger.info("Created choice with id: "+kC.getId());
		return kC;
	}
}
