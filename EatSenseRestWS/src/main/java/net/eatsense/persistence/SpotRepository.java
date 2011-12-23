package net.eatsense.persistence;

import net.eatsense.domain.Spot;

public class SpotRepository extends GenericRepository<Spot> {
	
	public SpotRepository() {
		super();
		super.clazz = Spot.class;
	}

}
