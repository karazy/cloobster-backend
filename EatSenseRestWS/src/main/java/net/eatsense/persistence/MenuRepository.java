package net.eatsense.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Query;

import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Menu;
import net.eatsense.domain.translation.MenuT;

public class MenuRepository extends LocalisedRepository<Menu, MenuT> {
	final static Class<Menu> entityClass = Menu.class;

	public MenuRepository() {
		super(Menu.class, MenuT.class);
	}
	
	public List<Menu> getActiveMenusForBusiness(Key<Business> businessKey) {
		return getActiveMenusForBusiness(businessKey, Optional.<Locale>absent());
	}
	
	public Iterable<Menu> iterateActiveMenusForBusiness(Key<Business> businessKey) {
		logger.info("business: {}", businessKey);
		return ofy().query(Menu.class).ancestor(businessKey).filter("active", true).fetch();
	}
	
	public Iterable<Key<Menu>> iterateActiveMenuKeysForBusiness(Key<Business> businessKey) {
		logger.info("business: {}", businessKey);
		return ofy().query(Menu.class).ancestor(businessKey).filter("active", true).fetchKeys();
	}
	
	public List<Menu> getActiveMenusForBusinessAndArea(Key<Business> businessKey, long areaId) {
		logger.info("areaId: {}",areaId);
		Area area;
		try {
			area = ofy().get(new Key<Area>(businessKey, Area.class, areaId));
		} catch (NotFoundException e) {
			logger.warn("No area found with id: {}",areaId);
			return Collections.emptyList();
		}
		
		if(area.getMenus() == null || area.getMenus().isEmpty()) {
			return Collections.emptyList();
		}
		
		ArrayList<Menu> menus = new ArrayList<Menu>(ofy().get(area.getMenus()).values());
		
		for (Iterator<Menu> iterator = menus.iterator(); iterator.hasNext();) {
			Menu menu = iterator.next();
			if(!menu.isActive()) {
				iterator.remove();
			}
		}
		
		return menus;
	}

	/**
	 * @param businessKey
	 * @param optLocale
	 * @return
	 */
	public List<Menu> getActiveMenusForBusiness(Key<Business> businessKey,
			Optional<Locale> optLocale) {
		logger.info("business: {}, locale: {}", businessKey, optLocale);
		Query<Menu> query = ofy().query(Menu.class).ancestor(businessKey).filter("active", true);
		if(optLocale.isPresent()) {
			
			return loadAndApplyTranslations(query, optLocale.get());
		} else
			return query.list();
	}

	/**
	 * @param businessKey
	 * @param areaId
	 * @param optLocale
	 * @return
	 */
	public List<Menu> getActiveMenusForBusinessAndArea(
			Key<Business> businessKey, long areaId, Optional<Locale> optLocale) {		
		logger.info("business: {}, areaId: {}",businessKey, areaId);
		
		Area area;
		try {
			area = ofy().get(new Key<Area>(businessKey, Area.class, areaId));
		} catch (NotFoundException e) {
			logger.warn("No area found with id: {}",areaId);
			return Collections.emptyList();
		}
		
		if(area.getMenus() == null || area.getMenus().isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Menu> menus;
		if(optLocale.isPresent()) {
			menus = loadAndApplyTranslations(area.getMenus(), optLocale.get());
		}
		else
			menus = new ArrayList<Menu>(ofy().get(area.getMenus()).values());
		
		for (Iterator<Menu> iterator = menus.iterator(); iterator.hasNext();) {
			Menu menu = iterator.next();
			if(!menu.isActive()) {
				iterator.remove();
			}
		}
		
		return menus;
	}
}
