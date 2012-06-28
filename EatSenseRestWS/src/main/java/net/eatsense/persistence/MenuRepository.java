package net.eatsense.persistence;

import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Menu;

public class MenuRepository extends GenericRepository<Menu> {
	final static Class<Menu> entityClass = Menu.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	public MenuRepository() {
		super(Menu.class);
	}
	
	public List<Menu> getActiveMenusForBusiness(Key<Business> businessKey) {
		return ofy().query(Menu.class).ancestor(businessKey).filter("active", true).list();
	}
}
