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
import com.googlecode.objectify.NotFoundException;

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
			 List<Product> products = productRepo.getListByProperty("menu", menu.getKey() );
			 
			 List<ProductDTO> productDTOs = transformtoDto(products, true);
			 menuDTO.setTitle(menu.getTitle());
			 menuDTO.setProducts(productDTOs);
			 
			 menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}

	private List<ProductDTO> transformtoDto(List<Product> products, boolean skipId) {
		List<ProductDTO> productDTOs = new ArrayList<ProductDTO>();
		 for( Product p : products)	 {
			 ProductDTO dto = transformtoDto(p, skipId);
			 
			 productDTOs.add(dto);
		 }
		return productDTOs;
	}

	private ProductDTO transformtoDto(Product product, boolean skipId) {
		if(product == null)
			return null;
		ProductDTO dto = new ProductDTO();
		 
		 dto.setId(product.getId());		 
		 dto.setName( product.getName() );
		 dto.setLongDesc( product.getLongDesc() );
		 dto.setShortDesc( product.getShortDesc() );
		 dto.setPrice( product.getPrice() );
		 
			 dto.setChoices(retrieveChoicesForProduct(product, skipId));
		 
		return dto;
	}
	
	public Collection<ProductDTO> getAllProducts(Long restaurantId) {
		
		return transformtoDto(restaurantRepo.getChildren(Product.class, new Key<Restaurant>(Restaurant.class, restaurantId)), true );
	}
	
	public ProductDTO getProduct(Long restaurantId, Long id) {
		logger.info("Trying to get product id : " + id + " for restaurant : id");
		ProductDTO productDto = new ProductDTO(); 
		
		try {
			productDto = transformtoDto(productRepo.getByKey(new Key<Restaurant>(Restaurant.class ,restaurantId), id), false );
			
		} catch (NotFoundException e) {
			logger.error("Product not found with id "+id, e);
			return null;
		}
		
		
		
				
		return productDto;
	}
	
	private Collection<ChoiceDTO> retrieveChoicesForProduct(Product p, boolean skipId)
	{
		ArrayList<ChoiceDTO> choices = null;
		
		Map<Key<Choice>,Choice> result = null;
		
		if(p != null && p.getChoices() != null && !p.getChoices().isEmpty()) 
		  result = productRepo.getOfy().get(p.getChoices());
		
		if(result != null && !result.isEmpty())  {
			choices = new ArrayList<ChoiceDTO>();
			
			for (Choice choice : result.values())  {
				ChoiceDTO dto = new ChoiceDTO();
				
//				if(!skipId) {
					dto.setId(choice.getId());
//				}
				dto.setIncluded(choice.getIncludedChoices());
				dto.setMaxOccurence(choice.getMaxOccurence());
				dto.setMinOccurence(choice.getMinOccurence());
				dto.setOverridePrice(choice.getOverridePrice());
				
				dto.setPrice(choice.getPrice() == null ? 0 : choice.getPrice());
				dto.setText(choice.getText());
				
				if( choice.getAvailableChoices() != null && !choice.getAvailableChoices().isEmpty() ) {
					List<ProductOption> list = choice.getAvailableChoices();
//					if(skipId) {
						for (ProductOption productOption : list) {
							productOption.setId(null);
						}
//					}
					dto.setOptions(list);					
					
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
