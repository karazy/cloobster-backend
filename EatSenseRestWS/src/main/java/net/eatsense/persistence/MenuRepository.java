package net.eatsense.persistence;

import net.eatsense.domain.Menu;

public class MenuRepository extends GenericRepository<Menu> {
	final static Class<Menu> entityClass = Menu.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	public MenuRepository() {
		super(Menu.class);
	}
}
