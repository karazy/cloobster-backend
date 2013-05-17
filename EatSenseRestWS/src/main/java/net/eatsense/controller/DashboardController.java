package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Business;
import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.exceptions.DataConflictException;
import net.eatsense.persistence.DashBoarditemRepository;
import net.eatsense.representation.DashboardConfigDTO;
import net.eatsense.representation.DashboardItemDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

public class DashboardController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	public final static List<String> DEFAULT_ITEMS = ImmutableList.of(
			"infopages", "products","actions", "productsspecial",
			 "infopagesall","productsall","feedback");
	private final DashBoarditemRepository itemRepo;

	@Inject
	public DashboardController(DashBoarditemRepository itemRepo) {
		this.itemRepo = itemRepo;
	}
	
	/**
	 * @param locationKey
	 * @return 
	 */
	public List<DashboardItem> getItems(Key<Business> locationKey) {
		DashboardConfiguration config = itemRepo.getConfiguration(locationKey);
		
		if(config != null) {
			return new ArrayList<DashboardItem>(itemRepo.getByKeys(config.getItems()));
		}
		else {
			return itemRepo.getByParent(locationKey);
		}
	}
	
	public List<DashboardItem> getItemsForActiveFeatures(Business location) {
		DashboardConfiguration config = itemRepo.getConfiguration(location.getKey());
		List<DashboardItem> items;
		
		if(config != null) {
			items = new ArrayList<DashboardItem>(itemRepo.getByKeys(config.getItems()));
		}
		else {
			items = itemRepo.getByParent(location.getKey());
		}
		boolean isFeedbackDisabled = location.getDisabledFeatures().contains("feedback");
		boolean isProductsDisabled = location.getDisabledFeatures().contains("products");
		boolean isInfopagesDisabled = location.getDisabledFeatures().contains("infopages");
		boolean isServiceCallDisabled = location.getDisabledFeatures().contains("requests-call");
		
		// filter list by active features only
		for (Iterator<DashboardItem> iterator = items.iterator(); iterator.hasNext();) {
			DashboardItem dashboardItem = iterator.next();
			if(dashboardItem.getType().equals("feedback") && isFeedbackDisabled) {			
				iterator.remove();
			}
			else if(dashboardItem.getType().startsWith("products") && isProductsDisabled) {
				iterator.remove();
			}
			else if(dashboardItem.getType().startsWith("infopages") && isInfopagesDisabled) {				
				iterator.remove();
			}
			else if(dashboardItem.getType().equals("actions") && isServiceCallDisabled) {
				iterator.remove();
			}
		}
				
		return items;
	}
	
	public DashboardItem get(Key<Business> locationKey, long id) {
		try {
			return itemRepo.getById(locationKey, id);
		} catch (NotFoundException e) {
			logger.error("No DashboardItem found for location={}, id={}", locationKey, id);
			throw new net.eatsense.exceptions.NotFoundException("No dashboard item found with the specified id.");
		}
	}
	
	public DashboardItem createAndSave(Key<Business> locationKey, DashboardItemDTO itemData) {
		checkNotNull(locationKey, "locationKey was null");
		checkNotNull(itemData, "itemData was null");
		
		
		DashboardConfiguration config = itemRepo.getConfiguration(locationKey);
		if(config.getItems().size() > 9) {
			logger.error("Maximum number of DashboardItems reached, aborting creation. location={}", locationKey);
			throw new DataConflictException("Already 10 items in this location's dashboard.");
		}
		DashboardItem item = itemRepo.newEntity();
		item.setLocation(locationKey);
		Key<DashboardItem> itemKey = update(item, itemData);
		
		config.getItems().add(itemKey);
		itemRepo.saveOrUpdateConfiguration(config);
		
		return item;
	}
	
	public DashboardConfiguration getAndUpdateConfig(Key<Business> locationKey, DashboardConfigDTO configData) {
		DashboardConfiguration config = itemRepo.getConfiguration(locationKey);
		
		if(configData.getItemIds() != null) {
			config.setItems(itemRepo.getKeys(locationKey, configData.getItemIds()));
			itemRepo.saveOrUpdateConfiguration(config);
		}
		
		return config;
	}
	
	public Key<DashboardItem> update(DashboardItem item, DashboardItemDTO itemData) {
		checkNotNull(item, "item was null");
		checkNotNull(itemData, "itemData was null");
		
		item.setType(itemData.getType());
		item.setEntityIds(itemData.getEntityIds());
		
		return itemRepo.saveOrUpdate(item);
	}
	
	public DashboardItem getAndUpdate(Key<Business> locationKey, long id, DashboardItemDTO itemData) {
		checkNotNull(locationKey, "locationKey was null");
		
		DashboardItem item = get(locationKey, id);
		update(item, itemData);
		
		return item;
	}
	
	public void delete(Key<Business> locationKey, long id) {
		checkNotNull(locationKey, "locationKey was null");
		DashboardConfiguration config = itemRepo.getConfiguration(locationKey);
		if(config == null) {
			return;
		}
		
		Key<DashboardItem> itemKey = itemRepo.getKey(locationKey, id);
		if(config.getItems().remove(itemKey))
			itemRepo.saveOrUpdateConfiguration(config);
		itemRepo.delete(itemKey);
	}
	
	/**
	 * Handles default dashboard items creation during creation of a new Business in the store.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleNewLocationEvent(NewLocationEvent event) {
		createDefaultItems(event.getLocation().getKey());
	}

	/**
	 * @param locationKey
	 */
	public void createDefaultItems(Key<Business> locationKey) {
		checkNotNull(locationKey, "locationKey was null");
		
		logger.info("Adding default dashboard configuration for {}", locationKey);
		
		DashboardConfiguration config = new DashboardConfiguration();
		config.setLocation(locationKey);
		config.setName("dashboard");
		
		ArrayList<DashboardItem> dashboardItems = new ArrayList<DashboardItem>();
		
		//Create all default items
		for (String itemType : DEFAULT_ITEMS) {
			DashboardItem newItem = itemRepo.newEntity();
			
			newItem.setType(itemType);
			newItem.setId(itemRepo.allocateId(locationKey));
			newItem.setLocation(locationKey);

			dashboardItems.add(newItem);
			config.getItems().add(newItem.getKey());
		}
		
		itemRepo.saveOrUpdateAsync(dashboardItems);
		itemRepo.saveOrUpdateConfiguration(config);
	}
}
