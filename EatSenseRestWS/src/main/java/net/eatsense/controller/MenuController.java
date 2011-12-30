package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.eatsense.domain.Product;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Menu;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
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
	public Collection<MenuDTO> getMenus(long restaurantId){
		if(restaurantId == 0 )
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
				 
				 productDTOs.add(dto);
			 }
			 menuDTO.setTitle(menu.getTitle());
			 menuDTO.setProducts(productDTOs);
			 
			 menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}
	
}
