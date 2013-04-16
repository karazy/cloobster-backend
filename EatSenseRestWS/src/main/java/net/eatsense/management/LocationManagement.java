package net.eatsense.management;

import java.awt.ImageCapabilities;
import java.util.List;
import java.util.Map;

import net.eatsense.controller.ImageController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.OfyService;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class LocationManagement {
	
	private final OfyService ofyService;
	private final ImageController imageController;

	@Inject
	public LocationManagement(OfyService ofyService, ImageController imageController) {
		this.ofyService = ofyService;
		this.imageController = imageController;
	}
	
	public Business copyLocationAndAllEntities(long fromLocationId, long newOwnerAccountId) {
		Objectify ofy = ofyService.ofy();		
		
		Key<Business> originalLocationKey = ofyService.keys().create(Business.class, fromLocationId);
		Account newOwnerAccount = ofy.get(Account.class, newOwnerAccountId);
		Business location = ofy.get(originalLocationKey);
		
		// Copy all image blobs for this location
		location.setImages(imageController.copyImages(location.getImages()).getImages());
		
		// Set new id and company at location.
		location.setId(ofyService.factory().allocateId(Business.class));
		location.setCompany(newOwnerAccount.getCompany());
		
		Key<Business> newLocationKey = location.getKey();
				
		Map<Key<Area>, Area> allAreas = Maps.newHashMap();
		Map<Long, Key<Area>> oldToNewAreaIdsMap = Maps.newHashMap();
		
		Iterable<Spot> spotsIterable = ofy.query(Spot.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Menu> menusIterable = ofy.query(Menu.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Area> areasIterable = ofy.query(Area.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		Iterable<Product> productsIterable = ofy.query(Product.class).ancestor(originalLocationKey).filter("trash", false).fetch();
		
		Map<Key<Menu>,Menu> allMenus = Maps.newHashMap();
		Map<Long, Key<Menu>> oldToNewMenuIdsMap = Maps.newHashMap();
		
		for (Menu menu : menusIterable) {
			long newMenuId = ofyService.factory().allocateId(newLocationKey, Menu.class);
			Key<Menu> newMenuKey = ofyService.keys().create(newLocationKey, Menu.class, newMenuId);
			oldToNewMenuIdsMap.put(menu.getId(), newMenuKey);
			menu.setId(newMenuId);
			menu.setBusiness(newLocationKey);
			allMenus.put(newMenuKey, menu);
		}				
		
		for (Area area : areasIterable) {
			// allocate new id for area, create key. add key to map with old id
			long newAreaId = ofyService.factory().allocateId(newLocationKey, Area.class);
			Key<Area> newAreaKey = ofyService.keys().create(newLocationKey, Area.class, newAreaId);
			oldToNewAreaIdsMap.put(area.getId(), newAreaKey);
			// set new id on area, add area to map with new key
			area.setId(newAreaId);
			area.setBusiness(newLocationKey);
			List<Key<Menu>> newMenuKeys = Lists.newArrayList(); 
			for (Key<Menu> oldMenuKey : area.getMenus()) {
				newMenuKeys.add(oldToNewMenuIdsMap.get(oldMenuKey.getId()));
			}
			area.setMenus(newMenuKeys);
			
			allAreas.put(newAreaKey, area);
		}
		
		List<Spot> allSpots = Lists.newArrayList();
		
		for (Spot spot : spotsIterable) {
			long newSpotId = ofyService.factory().allocateId(newLocationKey, Spot.class);
			spot.setBusiness(newLocationKey);
			spot.setId(newSpotId);
			spot.generateBarcode();
			Key<Area> newAreaKey = oldToNewAreaIdsMap.get(spot.getArea().getId());
			spot.setArea(newAreaKey);
			
			allSpots.add(spot);
		}
		
		return null;
	}
}
