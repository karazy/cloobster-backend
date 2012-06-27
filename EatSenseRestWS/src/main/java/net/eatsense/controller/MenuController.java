package net.eatsense.controller;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.Business;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.validation.CreationChecks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
	private Validator validator;
	private ChoiceRepository choiceRepo;

	@Inject
	public MenuController(MenuRepository mr, ProductRepository pr, ChoiceRepository cr, Transformer trans, Validator validator) {
		this.choiceRepo = cr;
		this.menuRepo = mr;
		this.productRepo = pr;
		this.transform = trans;
		this.validator = validator;
	}
	
	/**
	 * Return all menus with corresponding products of a given business.
	 * 
	 * @param business entity id of the business
	 * @return list of menus with products
	 */
	public Collection<MenuDTO> getMenusWithProducts(Business business){
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		if(business == null )
			return menuDTOs;
		
		List<Menu> menus = menuRepo.getByParent( business );
		List<ProductDTO> products = transform.productsToDto(productRepo.getByParentOrdered(business, "name"));
		
		ListMultimap<Long, ProductDTO> menuToProductsMap = ArrayListMultimap.create();
		
		for (ProductDTO productDTO : products) {
			menuToProductsMap.put(productDTO.getMenuId(), productDTO);
		}

		for ( Menu menu : menus) {
			MenuDTO menuDTO = new MenuDTO(menu);
			// Get products for this menu from the product map.		
			menuDTO.setProducts(menuToProductsMap.get(menu.getId())); 
			menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}
	
	/**
	 * @param business 
	 * @return List of transfer objects without embedded Products.
	 */
	public List<MenuDTO> getMenus(Business business) {
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		if(business == null )
			return menuDTOs;
		List<Menu> menus = menuRepo.getByParent( business );
		
		for ( Menu menu : menus) {
			MenuDTO menuDTO = new MenuDTO(menu);
 
			menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}
	
	/**
	 * @param business
	 * @param id
	 * @return
	 */
	public Menu getMenu(Business business, long id) {
		checkNotNull(business, "business was null");
		checkArgument(id != 0, "id was 0");
		
		Menu menu;
		try {
			menu = menuRepo.getById(business.getKey(), id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		return menu;
	}
	
	/**
	 * Load and transform Menu entity with the supplied id,
	 * also loads associated products.
	 * 
	 * @param business
	 * @param id
	 * @return
	 */
	public MenuDTO getMenuDTOWithProducts(Business business, long id) {
		Menu menu = getMenu(business, id);
		MenuDTO menuData = new MenuDTO(menu);
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		for (Product product : productRepo.getListByProperty("menu", menu)) {
			productsData.add(new ProductDTO(product));
		}
		menuData.setProducts(productsData);
		
		return menuData;
	}
	
	/**
	 * Create and save new Menu entity populated with
	 * data from the supplied transfer object.
	 * 
	 * @param business
	 * @param menuData Transfer object
	 * @return
	 */
	public MenuDTO createMenu(Business business, MenuDTO menuData) {
		checkNotNull(business, "business was null");
		checkNotNull(menuData, "menuData was null");
				
		Menu menu = menuRepo.newEntity();
		menu.setBusiness(business.getKey());
		menuData = updateMenu(menu, menuData);
		
		return menuData;
	}

	/**
	 * Update and save changes to the Menu entity.
	 * 
	 * @param menu
	 * @param menuData
	 * @return
	 */
	public MenuDTO updateMenu(Menu menu, MenuDTO menuData) {
		checkNotNull(menu, "menu was null");
		checkNotNull(menuData, "menuData was null");
		
		Set<ConstraintViolation<MenuDTO>> violationSet = validator.validate(menuData, CreationChecks.class);
		
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<MenuDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		menu.setDescription(menuData.getDescription());
		menu.setOrder(menuData.getOrder());
		menu.setTitle(menuData.getTitle());
		
		if(menu.isDirty())
			menuRepo.saveOrUpdate(menu);
		
		return new MenuDTO(menu);
	}
	
	/**
	 * Delete Menu entity from the datastore.
	 * 
	 * @param business
	 * @param id
	 */
	public void deleteMenu(Business business, long id) {
		checkNotNull(business, "business was null");
		checkArgument(id != 0, "id was 0");
		
		menuRepo.delete(menuRepo.getKey(business.getKey(), id));
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
	 * Retrieve Product entity.
	 * 
	 * @param businessKey
	 * @param id
	 * @return Product entity with the given id.
	 */
	public Product getProduct(Key<Business> businessKey, long id) {
		checkNotNull(businessKey, "businessKey was null");
		checkArgument(id != 0, "id was 0");
		
		try {
			return productRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
	
	/**
	 * @param business
	 * @param menuId
	 * @return
	 */
	public List<ProductDTO> getProductsForMenu(Business business, long menuId) {
		checkNotNull(business, "business was null");
		checkArgument(menuId != 0, "menuId was 0");
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		for (Product product : productRepo.getListByProperty("menu", menuRepo.getKey(business.getKey(), menuId))) {
			productsData.add(new ProductDTO(product));
		}
		
		return productsData;
	}
	
	/**
	 * Retrieve and transform Product entity.
	 * 
	 * @param business
	 * @param id
	 * @return product DTO
	 */
	public ProductDTO getProductWithChoices(Key<Business> businessKey, long id) {
		return transform.productToDto(getProduct(businessKey,id));
	}
	
	/**
	 * @param business
	 * @param productData
	 * @return
	 */
	public ProductDTO createProduct(Business business, ProductDTO productData) {
		checkNotNull(business, "business was null");
		checkNotNull(productData, "productData was null");
		
		Product product = productRepo.newEntity();
		product.setBusiness(business.getKey());
		productData = updateProduct(product, productData);
		
		return productData;
	}

	/**
	 * @param product
	 * @param productData
	 * @return
	 */
	public ProductDTO updateProduct(Product product, ProductDTO productData) {
		checkNotNull(product, "product was null");
		checkNotNull(productData, "productData was null");
		
		Set<ConstraintViolation<ProductDTO>> violationSet = validator.validate(productData);
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<ProductDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		if(productData.getMenuId() != null)
			product.setMenu(menuRepo.getKey(product.getBusiness(), productData.getMenuId()));
		
		product.setLongDesc(productData.getLongDesc());
		product.setName(productData.getName());
		product.setOrder(productData.getOrder());
		product.setPrice(productData.getPrice());
		product.setShortDesc(productData.getShortDesc());
		
		if(product.isDirty())
			productRepo.saveOrUpdate(product);
		
		return new ProductDTO(product);
	}
	
	/**
	 * @param businessKey
	 * @param id
	 * @return Entity
	 */
	public Choice getChoice(Key<Business> businessKey, long id) {
		checkNotNull(businessKey, "businessKey was null");
		checkArgument(id != 0, "id was 0");
		
		Choice choice;
		try {
			choice = choiceRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		return choice;
	}
	
	/**
	 * Retrieve all Choices saved for the Product given by the id.
	 * 
	 * @param businessKey
	 * @param productId
	 * @return
	 */
	public List<ChoiceDTO> getChoicesForProduct(Key<Business> businessKey, long productId) {
		checkNotNull(businessKey, "businessKey was null");
		checkArgument(productId != 0,  "productId was 0");
		
		ArrayList<ChoiceDTO> choiceDtos = new ArrayList<ChoiceDTO>();
		Product product;
		try {
			product = getProduct(businessKey, productId);
		} catch (net.eatsense.exceptions.NotFoundException e) {
			logger.warn("No product found with id {}", productId);
			return choiceDtos;
		}
		
		if(product.getChoices() != null && !product.getChoices().isEmpty()) {
			Collection<Choice> choices = choiceRepo.getByKeys(product.getChoices());
			
			for (Choice choice : choices)  {
				choiceDtos.add( new ChoiceDTO(choice , productId) );
			}
		}
			
		return choiceDtos;
	}
	
	/**
	 * Create and save new Choice entity.
	 * 
	 * @param business
	 * @param choiceData
	 * @return
	 */
	public ChoiceDTO createChoice(Business business, ChoiceDTO choiceData) {
		checkNotNull(business, "business was null");
		checkNotNull(choiceData, "choiceData was null");
		
		Choice choice = choiceRepo.newEntity();
		choice.setBusiness(business.getKey());
		
		choiceData = updateChoice(choice, choiceData);
		
		return choiceData;
	}

	/**
	 * Update Choice data and add add to Product specified by the id if not already added.
	 * 
	 * @param choice
	 * @param choiceData
	 * @return
	 */
	public ChoiceDTO updateChoice(Choice choice, ChoiceDTO choiceData) {
		checkNotNull(choice, "choice was null");
		checkNotNull(choiceData, "choicedata was null");
		
		Set<ConstraintViolation<ChoiceDTO>> violationSet = validator.validate(choiceData);
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<ChoiceDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		choice.setIncludedChoices(choiceData.getIncluded());
		choice.setMaxOccurence(choiceData.getMaxOccurence());
		choice.setMinOccurence(choiceData.getMinOccurence());
		choice.setOptions(choiceData.getOptions());
		choice.setOverridePrice(choiceData.getOverridePrice());
		
		if(choiceData.getParent() != null)
			choice.setParentChoice(choiceRepo.getKey(choice.getBusiness(), choiceData.getParent()));
		choice.setPrice(choiceData.getPrice());
		
		Key<Product> newProductKey = productRepo.getKey(choice.getBusiness(), choiceData.getProductId());
		
		if(choice.getProduct() == null) {
			// Only set the Product if this was the first Product to be set.
			choice.setProduct(newProductKey);	
		}
		
		choice.setText(choiceData.getText());
		
		Key<Choice> choiceKey;
		if(choice.isDirty()) {
			choiceKey = choiceRepo.saveOrUpdate(choice);
		} else {
			choiceKey = choice.getKey();			
		}
		
		Product product = getProduct(choice.getBusiness(), choiceData.getProductId().longValue());
		
		// Add the Choice to the product and check if it was added.
		if(product.addChoice(choiceKey)) {
			// Only save if the choice was not already in the list.
			productRepo.saveOrUpdate(product);
		}
		
		return new ChoiceDTO(choice);
	}

}
