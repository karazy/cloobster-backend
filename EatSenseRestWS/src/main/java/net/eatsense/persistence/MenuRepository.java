package net.eatsense.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Menu;

public class MenuRepository extends GenericRepository<Menu> {
	final static Class<Menu> entityClass = Menu.class;

	public MenuRepository() {
		super(Menu.class);
	}
	
	public List<Menu> getActiveMenusForBusiness(Key<Business> businessKey) {
		return ofy().query(Menu.class).ancestor(businessKey).filter("active", true).list();
	}
	
	public List<Menu> getActiveMenusForBusinessAndArea(Key<Business> businessKey, long areaId) {
		Area area = ofy().get(new Key<Area>(businessKey, Area.class, areaId));
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
}
