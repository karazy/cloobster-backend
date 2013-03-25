package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.domain.Business;
import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;
import net.eatsense.exceptions.DataConflictException;
import net.eatsense.persistence.DashBoarditemRepository;
import net.eatsense.representation.DashboardConfigDTO;
import net.eatsense.representation.DashboardItemDTO;

public class DashboardController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
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
		
		itemRepo.delete(itemRepo.getKey(locationKey, id));
	}
}
