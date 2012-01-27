package net.eatsense.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Controller for displaying Menu and Product listings, as well as options and extras for food products.
 * 
 * @author Nils Weiher
 *
 */
public class MenuController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private RestaurantRepository restaurantRepo;
	private MenuRepository menuRepo;
	private ProductRepository productRepo;

	@Inject
	public MenuController(RestaurantRepository r, MenuRepository mr, ProductRepository pr) {
		this.restaurantRepo = r;
		this.menuRepo = mr;
		this.productRepo = pr;
	}
	
	/**
	 * Return all menus with corresponding products of a given restaurant.
	 * 
	 * @param restaurantId entity id of the restaurant
	 * @return list of menus with products
	 */
	public Collection<MenuDTO> getMenus(Long restaurantId){
		if(restaurantId == null )
			throw new IllegalArgumentException("Invalid restaurant key specified.");
		
		
		logger.info("Returning menus of restaurant id: " + restaurantId);
		Key<Restaurant> restaurant = new Key<Restaurant>(Restaurant.class, restaurantId);
		
		List<Menu> menus = restaurantRepo.getChildren(Menu.class, restaurant);
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		
		for ( Menu menu : menus) {
			 MenuDTO menuDTO = new MenuDTO();
			 // Query for a list of all products associated with this menu
			 List<Product> products = menuRepo.getChildren(Product.class, menu.getKey() );
			 List<ProductDTO> productDTOs = new ArrayList<ProductDTO>();
			 for( Product p : products)	 {
				 ProductDTO dto = new ProductDTO();
				 dto.setName( p.getName() );
				 dto.setLongDesc( p.getLongDesc() );
				 dto.setShortDesc( p.getShortDesc() );
				 dto.setPrice( p.getPrice() );
				 
				 dto.setChoices(retrieveChoicesForProduct(p));
				 
				 productDTOs.add(dto);
			 }
			 menuDTO.setTitle(menu.getTitle());
			 menuDTO.setProducts(productDTOs);
			 
			 menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}
	
	private Collection<ChoiceDTO> retrieveChoicesForProduct(Product p)
	{
		ArrayList<ChoiceDTO> choices = null;
		
		Map<Key<Choice>,Choice> result = null;
		
		if(p != null && p.getChoices() != null && !p.getChoices().isEmpty()) 
		  result = productRepo.getOfy().get(p.getChoices());
		
		if(result != null && !result.isEmpty())  {
			choices = new ArrayList<ChoiceDTO>();
			
			for (Choice choice : result.values())  {
				ChoiceDTO dto = new ChoiceDTO();
				
				dto.setId(choice.getId());
				dto.setIncluded(choice.getIncludedChoices());
				dto.setMaxOccurence(choice.getMaxOccurence());
				dto.setMinOccurence(choice.getMinOccurence());
				dto.setOverridePrice(choice.getOverridePrice());
				dto.setPrice(choice.getPrice());
				dto.setText(choice.getText());
				
				if( choice.getAvailableChoices() != null && !choice.getAvailableChoices().isEmpty() ) {
					dto.setOptions(choice.getAvailableChoices());
					
				}
				else if (choice.getAvailableProducts() != null && !choice.getAvailableProducts().isEmpty()) {
					ArrayList<ProductOption> options = new ArrayList<ProductOption>();
					Map<Key<Product>,Product> products =  productRepo.getOfy().get(choice.getAvailableProducts());
					
					for (Product choiceProduct : products.values() ) {
						options.add(new ProductOption(choiceProduct.getName(), choiceProduct.getPrice(), choiceProduct.getId()));
					}
					
					dto.setOptions(options);
				}
				
				choices.add( dto );
				
			}
		}
		
		return choices; 		
	}
	
}
