package net.eatsense.controller;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.validation.CreationChecks;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
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
	private final MenuRepository menuRepo;
	private final ProductRepository productRepo;
	private final Transformer transform;
	private final ValidationHelper validator;
	private final ChoiceRepository choiceRepo;
	private final ImageController imageCtrl;
	private final AreaRepository areaRepo;

	@Inject
	public MenuController(AreaRepository areaRepo, MenuRepository mr, ProductRepository pr, ChoiceRepository cr, Transformer trans, ValidationHelper validator, ImageController imageCtrl) {
		this.areaRepo = areaRepo;
		this.choiceRepo = cr;
		this.menuRepo = mr;
		this.productRepo = pr;
		this.transform = trans;
		this.imageCtrl = imageCtrl;
		this.validator = validator;
	}
	
	/**
	 * Return all menus with corresponding products of a given business.
	 * 
	 * @param business entity id of the business
	 * @return list of menus with products
	 */
	public Collection<MenuDTO> getMenusWithProducts(Key<Business> businessKey, long areaId){
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		if(businessKey == null)
			return menuDTOs;
		
		List<Menu> menus;
		
		if(areaId == 0) {
			menus = menuRepo.getActiveMenusForBusiness(businessKey);
		}
		else {
			menus = menuRepo.getActiveMenusForBusinessAndArea(businessKey, areaId);
		}
		
		if(menus.isEmpty()) {
			return menuDTOs;
		}
		
		List<ProductDTO> products = transform.productsToDtoWithChoices(productRepo.getActiveProductsForBusiness(businessKey));
		
		ListMultimap<Long, ProductDTO> menuToProductsMap = ArrayListMultimap.create();
		Map<Long, ProductDTO> productsMap = new HashMap<Long, ProductDTO>();
		
		for (ProductDTO productDTO : products) {
			menuToProductsMap.put(productDTO.getMenuId(), productDTO);
			productsMap.put(productDTO.getId(), productDTO);
		}

		for ( Menu menu : menus) {
			
			if(menu.getProducts() != null) {
				// Build product order.
				int index = 0;
				for (Key<Product> productKey : menu.getProducts()) {
					ProductDTO productDTO = productsMap.get(productKey.getId());
					
					if(productDTO != null) {
						productDTO.setOrder(index);
					}
					
					index++;
				}
			}
			
			MenuDTO menuDTO = new MenuDTO(menu);
			// Get products for this menu from the product map.
			
			menuDTO.setProducts(menuToProductsMap.get(menu.getId())); 
			menuDTOs.add(menuDTO);
		}
		
		return menuDTOs;
	}
	
	public List<MenuDTO> getMenusForArea(Key<Business> locationKey, long areaId) {
		List<MenuDTO> menuDTOs = new ArrayList<MenuDTO>();
		
		List<Menu> menus;
		
		if(areaId == 0) {
			menus = menuRepo.getActiveMenusForBusiness(locationKey);
		}
		else {
			menus = menuRepo.getActiveMenusForBusinessAndArea(locationKey, areaId);
		}
		
		for ( Menu menu : menus) {
			MenuDTO menuDTO = new MenuDTO(menu);
 
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
			if(!product.isTrash()) {
				productsData.add(new ProductDTO(product));
			}
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
		
		// Find welcome Area for this Location
		List<Area> welcomeAreas = areaRepo.getListByParentAndProperty(business.getKey(), "welcome", true);
		if(welcomeAreas.size() != 1) {
			logger.warn("{} has no or more than one welcome Area!", business.getKey());
		}
		else {
			Area welcomeArea = welcomeAreas.get(0);
			if(welcomeArea.getMenus() == null)
				welcomeArea.setMenus(new ArrayList<Key<Menu>>());
			// Add Menu and save welcome Area
			welcomeArea.getMenus().add(menu.getKey());
			areaRepo.saveOrUpdate(welcomeArea);
		}
		
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
		
		validator.validate(menuData, CreationChecks.class);
		
		menu.setDescription(menuData.getDescription());
		menu.setOrder(menuData.getOrder());
		menu.setTitle(menuData.getTitle());
		menu.setActive(menuData.isActive());
		
		if(!menuData.getProductIds().isEmpty()) {
			menu.setProducts(new ArrayList<Key<Product>>());
		}
		
		// Add product ids for odering.		
		for( Long productId : menuData.getProductIds()) {
			Key<Product> productKey = productRepo.getKey(menu.getBusiness(), productId);
			
			if(!menu.getProducts().contains(productKey)) {
				menu.getProducts().add(productKey);
			}
		}
		
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
		
		Key<Menu> menuKey = menuRepo.getKey(business.getKey(), id);
		List<Product> productList = productRepo.getListByParentAndProperty(business.getKey(), "menu", menuKey);
		// Get all products associated with the menu and set the link property to null.
		for (Product product : productList) {
			product.setMenu(null);
		}
		productRepo.saveOrUpdate(productList);
		
		menuRepo.delete(menuKey);
	}

	/**
	 * Retrieve all products saved for the given business.
	 * 
	 * @param business
	 * @return
	 */
	public Collection<ProductDTO> getProductsWithChoices(Business business) {
		Iterable<Key<Menu>> activeMenus = menuRepo.iterateActiveMenuKeysForBusiness(business.getKey());
		Iterable<Product> products = productRepo.iterateActiveProductsForBusiness(business.getKey());
		
		// Create set to check, if the menu key belonged to an active menu.
		ImmutableSet<Key<Menu>> menuSet = ImmutableSet.copyOf(activeMenus);
		
		List<Product> activeProducts = new ArrayList<Product>();
		for (Product product : products) {
			if(menuSet.contains(product.getMenu())) {
				// Only add to active products if the menu is in the active set.
				activeProducts.add(product);
			}
		}
		
		return transform.productsToDtoWithChoices(activeProducts);
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
	 * @param noMenu If true list Products with no menu associated.
	 * @return
	 */
	public List<ProductDTO> getProductsForMenu(Business business, long menuId) {
		checkNotNull(business, "business was null");
		checkArgument(menuId != 0, "menuId was 0");
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		Key<Menu> menuKey = menuRepo.getKey(business.getKey(), menuId);
		for (Product product : productRepo.getListByProperty("menu", menuKey )) {
			if(!product.isTrash()) {
				productsData.add(new ProductDTO(product));
			}
		}
		
		return productsData;
	}
	
	/**
	 * @param business
	 * @param menuId
	 * @param noMenu If true list Products with no menu associated.
	 * @return
	 */
	public List<ProductDTO> getProductsWithNoMenu(Business business) {
		checkNotNull(business, "business was null");
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		for (Product product : productRepo.getListByParentAndProperty(business.getKey(), "menu", null )) {
			if(!product.isTrash()) {
				productsData.add(new ProductDTO(product));
			}
		}
		
		return productsData;
	}
	
	/**
	 * Get all Product entities using this choice.
	 * 
	 * @param business
	 * @param menuId
	 * @return List of Product transfer objects.
	 */
	public List<ProductDTO> getProductsForChoice(Business business, long choiceId) {
		checkNotNull(business, "business was null");
		checkArgument(choiceId != 0, "choiceId was 0");
		
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		for (Product product : productRepo.getListByParentAndProperty(business.getKey(), "choices", choiceRepo.getKey(business.getKey(), choiceId))) {
			if(!product.isTrash()) {
				productsData.add(new ProductDTO(product));
			}
		}
		
		return productsData;
	}
	
	/**
	 * Get all Product entities for this Business.
	 * 
	 * @param business
	 * @param menuId
	 * @return List of Product transfer objects.
	 */
	public List<ProductDTO> getProducts(Business business) {
		checkNotNull(business, "business was null");
		List<ProductDTO> productsData = new ArrayList<ProductDTO>();
		
		for (Product product : productRepo.getByParent(business.getKey())) {
			if(!product.isTrash()) {
				productsData.add(new ProductDTO(product));
			}
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
		
		validator.validate(productData, Default.class);
		
		if(productData.getMenuId() != null)
			product.setMenu(menuRepo.getKey(product.getBusiness(), productData.getMenuId()));
		
		product.setLongDesc(productData.getLongDesc());
		product.setName(productData.getName());
		product.setOrder(productData.getOrder());
		product.setPrice(productData.getPriceMinor());
		product.setShortDesc(productData.getShortDesc());
		product.setActive(productData.isActive());
		product.setSpecial(productData.isSpecial());
		product.setHideInDashboard(productData.isHideInDashboard());
		
		if(productData.getChoices() != null) {
			// Update Choices
			ArrayList<Key<Choice>> choices = new ArrayList<Key<Choice>>();
			for (ChoiceDTO choice : productData.getChoices()) {
				if(choice.getId() == null) {
					// Create choice but set a flag, that we dont load and update the product for each new choice.
					choice = createChoice(product.getBusiness(), choice, true);
				}
				choices.add(choiceRepo.getKey(product.getBusiness(),choice.getId()));
			}
			product.setChoices(choices);
		}
		
		if(product.isDirty())
			productRepo.saveOrUpdate(product);
		
		// Return the choices sent to return the updated state.
		ProductDTO productDTO = new ProductDTO(product);
		productDTO.setChoices(productData.getChoices());
		return productDTO;
	}
	
	/**
	 * Mark a Product for deletion.
	 * 
	 * @param product
	 */
	public void trashProduct(Product product, Account account) {
		checkNotNull(product, "product was null");
		checkArgument(!product.isTrash(), "product already in trash");
		
		product.setActive(false);
		productRepo.trashEntity(product, account.getLogin());
	}
	
	/**
	 * Create or override the image for a Product entity.
	 * 
	 * @param account
	 * @param product
	 * @param imageData
	 * @return
	 */
	public ImageDTO updateProductImage(Account account, Product product, ImageDTO imageData) {
		checkNotNull(account, "account was null");
		checkNotNull(product, "product was null");
		checkNotNull(imageData, "imageData was null");
		
		// For the moment we only have one image per info page.
		// Always override this image.
		imageData.setId("image");
		
		UpdateImagesResult result = imageCtrl.updateImages(account, product.getImages(), imageData);
		
		if(result.isDirty()) {
			product.setImages(result.getImages());
			productRepo.saveOrUpdate(product);
		}
		
		return result.getUpdatedImage();
	}
	
	/**
	 * Remove the image from the Product entity and from the blobstore.
	 * 
	 * @param product
	 * @return
	 */
	public boolean removeProductImage(Product product) {
		checkNotNull(product, "product was null");
		
		UpdateImagesResult result = imageCtrl.removeImage("image", product.getImages());
		
		if(result.isDirty()) {
			product.setImages(result.getImages());
			productRepo.saveOrUpdate(product);
		}
		
		return result.isDirty();
	}
	
	/**
	 * Update multiple products at once.
	 * 
	 * @return List containing the updated Product entities.
	 */
	public List<Product> getAndUpdateProducts(Key<Business> locationKey, List<Long> ids, Boolean active, Boolean special, Boolean hideInDashboard) {
		checkNotNull(locationKey, "locationKey was null");
		checkNotNull(ids, "ids were null");

		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Product> products = productRepo.getByKeys(productRepo.getKeys(locationKey,
				ids));

		// De-/Activate all spots
		for (Product product : products) {
			if(active != null) {
				product.setActive(active);
			}
			if(special != null) {
				product.setSpecial(special);
			}
			if(hideInDashboard != null) {
				product.setHideInDashboard(hideInDashboard);
			}
		}

		productRepo.saveOrUpdate(products);

		return new ArrayList<Product>(products);
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
	 * Retrieve all Choices saved for the Product given by the id,
	 * or all choices for the business if productId is 0.
	 * 
	 * @param businessKey
	 * @param productId
	 * @return
	 */
	public List<ChoiceDTO> getChoices(Key<Business> businessKey, long productId) {
		checkNotNull(businessKey, "businessKey was null");

		ArrayList<ChoiceDTO> choiceDtos = new ArrayList<ChoiceDTO>();
		Collection<Choice> choices = null;
		if(productId != 0) {
			// Load only the choices for the given productId.
			Product product;
			try {
				product = getProduct(businessKey, productId);
			} catch (net.eatsense.exceptions.NotFoundException e) {
				logger.warn("No product found with id {}", productId);
				return choiceDtos;
			}
			
			if(product.getChoices() != null && !product.getChoices().isEmpty()) {
				choices = choiceRepo.getByKeys(product.getChoices());
			}
		}
		else {
			// Load all choices for this business.
			choices = choiceRepo.getByParent(businessKey);
		}
		
		if(choices != null) {
			for (Choice choice : choices)  {
				choiceDtos.add( new ChoiceDTO(choice , productId) );
			}
		}
		return choiceDtos;
	}
	
	/**
	 * Create and save new Choice entity and add the Choice to the product.
	 * (specifed by productId in choiceData).
	 * 
	 * @param businessKey
	 * @param choiceData
	 * @return
	 */
	public ChoiceDTO createChoice(Key<Business> businessKey, ChoiceDTO choiceData) {
		return createChoice(businessKey, choiceData, false);
	}
	
	/**
	 * Create and save new Choice entity.
	 * 
	 * @param businessKey
	 * @param choiceData
	 * @param dontUpdateProduct 
	 * @return
	 */
	public ChoiceDTO createChoice(Key<Business> businessKey, ChoiceDTO choiceData, boolean dontUpdateProduct) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(choiceData, "choiceData was null");
		
		Choice choice = choiceRepo.newEntity();
		choice.setBusiness(businessKey);
		
		choiceData = updateChoice(choice, choiceData);
		
		if(choice.getProduct() != null && !dontUpdateProduct) {
			Product product = productRepo.getByKey(choice.getProduct());
			
			// Add the Choice to the product and check if it was added.
			if(product.addChoice(choiceRepo.getKey(choice))) {
				// Only save if the choice was not already in the list.
				productRepo.saveOrUpdate(product);
			}
		}
		
		return choiceData;
	}

	/**
	 * Update Choice data and add to Product specified by the id if not already added.
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
		choice.setOrder(choiceData.getOrder());
		choice.setPrice(choiceData.getPriceMinor());
		choice.setText(choiceData.getText());
		
		if(choice.getProduct() == null && choiceData.getProductId() != null) {
			// Only set the Product if this was the first Product to be set.
			choice.setProduct(productRepo.getKey(choice.getBusiness(), choiceData.getProductId()));
		}
		
		if(choice.isDirty()) {
			choiceRepo.saveOrUpdate(choice);
		}
		
		return new ChoiceDTO(choice);
	}
	
	/**
	 * @param business
	 * @param choiceId
	 */
	public void deleteChoice(Key<Business> businessKey, long choiceId, long productId) {
		checkNotNull(businessKey, "businessKey was null");
		checkArgument(choiceId != 0, "choiceId was 0");
		
		Key<Choice> choiceKey = choiceRepo.getKey(businessKey, choiceId);
		List<Key<Choice>> choiceKeysToDelete = new ArrayList<Key<Choice>>();
		
		choiceKeysToDelete.add(choiceKey);
		
		if(productId != 0) {
			Product product = getProduct(businessKey, productId);
			if(product.getChoices() != null) {
				boolean dirty = false;
				if(!product.getChoices().remove(choiceKey) ) {
					logger.warn("Product has no choice with id: {}", choiceId);
				}
				else {
					dirty = true;
				}
				
				if(dirty)
					productRepo.saveOrUpdate(product);
			}
			else {
				// We received delete for choice from product, but no choices found.
				logger.warn("No choice found for product {}", productId);
			}
		}
		
		List<Key<Product>> productsUsingChoice = productRepo.getKeysByParentAndProperty(businessKey, "choices", choiceKey);
		
		if(productsUsingChoice.isEmpty()) {
			// No more products using this choice.
			// It's safe to delete it.
			choiceRepo.delete(choiceKeysToDelete);
		}
		else {
			if(productsUsingChoice.get(0).getId() == productId) {
				// This happens if the datastore is not yet consistently updated.
				// We still remove the choice.
				choiceRepo.delete(choiceKeysToDelete);
			}
		}
	}
}
