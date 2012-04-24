package net.eatsense.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Business;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.Transformer;

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
	private MenuRepository menuRepo;
	private ProductRepository productRepo;
	private Transformer transform;

	@Inject
	public MenuController(MenuRepository mr, ProductRepository pr, ChoiceRepository cr, Transformer trans) {
		this.menuRepo = mr;
		this.productRepo = pr;
		this.transform = trans;
	}
	
	/**
	 * Return all menus with corresponding products of a given business.
	 * 
	 * @param business entity id of the business
	 * @return list of menus with products
	 */
	public Collection<MenuDTO> getMenus(Business business){
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		if(business == null )
			return menuDTOs;
		
		List<Menu> menus = menuRepo.getByParent( business );
		for ( Menu menu : menus) {
			MenuDTO menuDTO = new MenuDTO();
			menuDTO.setTitle(menu.getTitle());
			// Query for a list of all products associated with this menu
			menuDTO.setProducts(transform.productsToDto(productRepo
					.getListByPropertyOrdered("menu", menu.getKey(), "name"))); 
			menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}

	/**
	 * Retrieve all products saved for the given business.
	 * 
	 * @param business
	 * @return
	 */
	public Collection<ProductDTO> getAllProducts(Business business) {
		return transform.productsToDto(productRepo.getByParent(business));
	}
	
	/**
	 * Retrieve the saved product.
	 * 
	 * @param business
	 * @param id
	 * @return product DTO
	 */
	public ProductDTO getProduct(Business business, Long id) {
		if(business == null)
			return null;
		try {
			return transform.productToDto(productRepo.getById(business.getKey(), id));
		} catch (NotFoundException e) {
			logger.error("Unable to retrieve product, no matching entity found");
			return null;
		}
	}

}
