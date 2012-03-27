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
	 * @param businessId entity id of the business
	 * @return list of menus with products
	 */
	public Collection<MenuDTO> getMenus(Long businessId){
		if(businessId == null )
			throw new IllegalArgumentException("Invalid business key specified.");
		
		
		logger.info("Returning menus of business id: " + businessId);
		Key<Business> business = new Key<Business>(Business.class, businessId);
		
		List<Menu> menus = menuRepo.getByParent( business);
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		
		for ( Menu menu : menus) {
			 MenuDTO menuDTO = new MenuDTO();
			 // Query for a list of all products associated with this menu
			 List<Product> products = productRepo.getListByPropertyOrdered("menu", menu.getKey(), "name");
			 
			 List<ProductDTO> productDTOs = transform.productsToDto(products);
			 menuDTO.setTitle(menu.getTitle());
			 menuDTO.setProducts(productDTOs);
			 
			 menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}

	public Collection<ProductDTO> getAllProducts(Long businessId) {
		
		return transform.productsToDto(productRepo.getByParent( new Key<Business>(Business.class, businessId)));
	}
	
	public ProductDTO getProduct(Long businessId, Long id) {
		logger.info("Trying to get product id : " + id + " for business : id");
		ProductDTO productDto = new ProductDTO(); 
		
		try {
			productDto = transform.productToDto(productRepo.getById(new Key<Business>(Business.class ,businessId), id));
			
		} catch (NotFoundException e) {
			logger.error("Product not found with id "+id, e);
			return null;
		}
			
		return productDto;
	}

}
