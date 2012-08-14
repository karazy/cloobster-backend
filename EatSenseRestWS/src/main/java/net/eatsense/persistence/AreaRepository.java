package net.eatsense.persistence;

import net.eatsense.domain.Area;

public class AreaRepository extends GenericRepository<Area> {
	public AreaRepository() {
		super(Area.class);
	}
}
