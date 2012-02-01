package net.eatsense.controller;

import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * @author Frederik Reifschneider
 *
 */
public class RestaurantController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private RestaurantRepository restaurantRepo;
	private MenuRepository menuRepo;
	private ProductRepository productRepo;
	
	@Inject
	public RestaurantController(RestaurantRepository r, MenuRepository mr, ProductRepository pr) {
		this.restaurantRepo = r;
		this.menuRepo = mr;
		this.productRepo = pr;
	}
	


}
