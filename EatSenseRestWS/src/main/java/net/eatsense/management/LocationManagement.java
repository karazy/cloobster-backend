
package net.eatsense.management;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;

import net.eatsense.controller.ImageController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Choice;
import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.InfoPage;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.domain.translation.InfoPageT;
import net.eatsense.persistence.OfyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class LocationManagement {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final OfyService ofyService;
	private final ImageController imageController;
	private final SubscriptionController subscriptionController;

	@Inject
	public LocationManagement(OfyService ofyService, ImageController imageController, SubscriptionController subscriptionController) {
		this.ofyService = ofyService;
		this.imageController = imageController;
		this.subscriptionController = subscriptionController;
	}
	
	/**
	 * Copy location and all relevant content as a new location.
	 * 
	 * @param fromLocationId
	 * @param newOwnerAccountId
	 * @return
	 */
	public Business copyLocationAndAllEntities(long fromLocationId, long newOwnerAccountId) {
		checkArgument(fromLocationId != 0, "fromLocationId was 0");
		checkArgument(newOwnerAccountId != 0, "newOwnerAccountId was 0");
		
		Objectify ofy = ofyService.ofy();		
		
		Key<Business> originalLocationKey = ofyService.keys().create(Business.class, fromLocationId);
		logger.info("Starting copy of {} and all relevant children", originalLocationKey);
		Account newOwnerAccount = ofy.get(Account.class, newOwnerAccountId);
		Business location = ofy.get(originalLocationKey);
		
		// Copy all image blobs for this location
		location.setImages(imageController.copyImages(location.getImages()).getImages());
		
		// Set new id and company at location.
		location.setId(ofyService.factory().allocateId(Business.class));
		location.setCompany(newOwnerAccount.getCompany());
		// Remove active subscription ( setting basic subscription later)
		location.setActiveSubscription(null);
		location.setName(location.getName() + " Copy");
		
		// Clear active channels, no need to save this
		if(location.getChannels() != null)
			location.getChannels().clear();
		
		// get the new key and add it to the owner account
		Key<Business> newLocationKey = location.getKey();
		if(newOwnerAccount.getBusinesses() == null) {
			newOwnerAccount.setBusinesses(Lists.<Key<Business>>newArrayList());
		}
		newOwnerAccount.getBusinesses().add(newLocationKey);
		
		logger.info("Querying all relevant child entities ...");
		// Query the datastore for all entities we want to copy
		Iterable<Spot> spotsIterable = ofy.query(Spot.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Menu> menusIterable = ofy.query(Menu.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Area> areasIterable = ofy.query(Area.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Product> productsIterable = ofy.query(Product.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Choice> choicesIterable = ofy.query(Choice.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<InfoPage> infoPagesIterable = ofy.query(InfoPage.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<InfoPageT> infoPageTransIterable = ofy.query(InfoPageT.class).ancestor(originalLocationKey).fetch();
		Iterable<DashboardItem> dashBoardItems = ofy.query(DashboardItem.class).ancestor(originalLocationKey).fetch();

		// Keep lists of all relevant entities for the copy
		List<Area> allAreas = Lists.newArrayList();
		List<Spot> allSpots = Lists.newArrayList();
		List<Menu> allMenus = Lists.newArrayList();
		List<Choice> allChoices = Lists.newArrayList();
		List<InfoPage> allInfoPages = Lists.newArrayList();
		List<InfoPageT> allInfoPageTrans = Lists.newArrayList();
		List<DashboardItem> allDashboardItems = Lists.newArrayList();
		
		// Map new product keys to the product because of relation to choices
		Map<Key<Product>,Product> allProducts = Maps.newHashMap();
		
		// Keep maps to map entity ids to the new keys
		Map<Long, Key<Area>> oldToNewAreaIdsMap = Maps.newHashMap();
		Map<Long, Key<Menu>> oldToNewMenuIdsMap = Maps.newHashMap();
		Map<Long, Key<Product>> oldToNewProductIdsMap = Maps.newHashMap();
		Map<Long, Key<DashboardItem>> oldToNewDashboardItemIdsMap = Maps.newHashMap();
		Map<Long, Key<InfoPage>> oldToNewInfoPageIdsMap = Maps.newHashMap();
		
		for (Menu menu : menusIterable) {
			menu.setBusiness(newLocationKey);
			Key<Menu> newMenuKey = menu.getKey();
			
			oldToNewMenuIdsMap.put(menu.getId(), newMenuKey);
			allMenus.add( menu);
		}
		
		for (Area area : areasIterable) {
			// set new location on area and create new key
			area.setBusiness(newLocationKey);
			Key<Area> newAreaKey = area.getKey();
			
			// update area -> menu association
			if(area.getMenus() != null) {
				List<Key<Menu>> newMenuKeys = Lists.newArrayList(); 
				for (Key<Menu> oldMenuKey : area.getMenus()) {
					newMenuKeys.add(oldToNewMenuIdsMap.get(oldMenuKey.getId()));
				}
				area.setMenus(newMenuKeys);			
			}
			
			oldToNewAreaIdsMap.put(area.getId(), newAreaKey);
			allAreas.add( area);
		}
		
		for (Spot spot : spotsIterable) {
			if(spot.getArea() == null) {
				logger.warn("Skipping Spot without Area. (id= {})",spot.getId());
				continue;
			}
			spot.setBusiness(newLocationKey);
			spot.generateBarcode();
			Key<Area> newAreaKey = oldToNewAreaIdsMap.get(spot.getArea().getId());
			spot.setArea(newAreaKey);
			
			allSpots.add(spot);
		}
		
		for (Product product : productsIterable) {
			// set new id and business key
			product.setBusiness(newLocationKey);
			// Set new menu key
			product.setMenu(oldToNewMenuIdsMap.get(product.getMenu().getId()));
			
			product.setImages(imageController.copyImages(product.getImages()).getImages());
			
			Key<Product> newProductKey = product.getKey();
	
			// Clear old choices list, we build it new when we iterate over all choices
			if(product.getChoices() != null)
				product.getChoices().clear();
			
			oldToNewProductIdsMap.put(product.getId(), newProductKey);
			allProducts.put(newProductKey, product);
		}
		
		for (Choice choice : choicesIterable) {
			
			choice.setBusiness(newLocationKey);
			Key<Product> newProductKey = oldToNewProductIdsMap.get(choice.getProduct().getId());
			choice.setProduct(newProductKey);
			Key<Choice> newChoiceKey = choice.getKey();
			
			Product product = allProducts.get(newProductKey);
			if(product.getChoices() == null) {
				logger.warn("Data inconsitency, choice belongs to product, but product had no choices.");
				product.setChoices(Lists.<Key<Choice>>newArrayList());
			}
			product.getChoices().add(newChoiceKey);
			
			allChoices.add(choice);
		}
		
		for (Menu menu : allMenus) {
			if(menu.getProducts() != null && !menu.getProducts().isEmpty()) {
				List<Key<Product>> newProductKeyList = Lists.newArrayList();
				// add the new key for each product to the list
				for (Key<Product> oldProductKey : menu.getProducts()) {
					newProductKeyList.add(oldToNewProductIdsMap.get(oldProductKey.getId()));
				}
			}
		}
		
		for (InfoPage infoPage : infoPagesIterable) {
			infoPage.setBusiness(newLocationKey);
			infoPage.setImages(imageController.copyImages(infoPage.getImages()).getImages());
			
			Key<InfoPage> newInfoPageKey = infoPage.getKey();
			
			oldToNewInfoPageIdsMap.put(infoPage.getId(), newInfoPageKey );
			allInfoPages.add( infoPage);
		}
		
		for (InfoPageT infoPageT : infoPageTransIterable) {
			infoPageT.setParent(oldToNewInfoPageIdsMap.get(infoPageT.getParent().getId()));
			allInfoPageTrans.add(infoPageT);
		}
		
		// get the dashboardconfig		
		DashboardConfiguration dashBoardConfig = ofy.get(ofyService.keys().create(originalLocationKey, DashboardConfiguration.class, "dashboard"));
		
		// set new location key for every item and populate key map
		for (DashboardItem dashboardItem : dashBoardItems) {
			dashboardItem.setLocation(newLocationKey);
			oldToNewDashboardItemIdsMap.put(dashboardItem.getId(), dashboardItem.getKey());
			allDashboardItems.add(dashboardItem);
		}
		
		// update the list on the dashboardconfig to keep the original order
		List<Key<DashboardItem>> newItemsList = Lists.newArrayList();
		for (Key<DashboardItem> oldItemKey : dashBoardConfig.getItems()) {
			newItemsList.add(oldToNewDashboardItemIdsMap.get(oldItemKey.getId()));
		}
		
		// set new item list and location
		dashBoardConfig.setLocation(newLocationKey);
		dashBoardConfig.setItems(newItemsList);
		
		FeedbackForm feedbackForm = null;
		// Copy feedback form if it was set
		if(location.getFeedbackForm() != null) {
			feedbackForm = ofy.get(location.getFeedbackForm());
			feedbackForm.setLocation(newLocationKey);
			feedbackForm.setId(ofyService.factory().allocateId(FeedbackForm.class));
			location.setFeedbackForm(feedbackForm.getKey());
		}
		
		logger.info("Setting basic subscription and saving location copy ...");
		subscriptionController.setBasicSubscription(location);
		// The location is saved now.
		logger.info("Saving areas ...");
		ofy.put(allAreas);
		logger.info("Saving spots ...");
		ofy.put(allSpots);
		logger.info("Saving menus ...");
		ofy.put(allMenus);
		logger.info("Saving products ...");
		ofy.put(allProducts.values());
		logger.info("Saving choices ...");
		ofy.put(allChoices);
		logger.info("Saving infopages ...");
		ofy.put(allInfoPages);
		logger.info("Saving infopage translations ... ");
		ofy.put(allInfoPageTrans);
		logger.info("Saving dashboard items ...");
		ofy.put(allDashboardItems);
		logger.info("Saving dashboard config ...");
		ofy.put(dashBoardConfig);
		logger.info("Saving owner account (login={]) ...", newOwnerAccount.getLogin());
		ofy.put(newOwnerAccount);
		
		if(feedbackForm != null) {
			logger.info("Saving feedback form ...");
			ofy.put(feedbackForm);
		}
		
		return location;
	}
}
