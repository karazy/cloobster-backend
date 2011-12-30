package net.eatsense.persistence;

import net.eatsense.domain.Menu;

public class MenuRepository extends GenericRepository<Menu> {

	public MenuRepository() {
		super();
		super.clazz = Menu.class;
	}
}
