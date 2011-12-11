package net.eatsense.persistence;

import net.eatsense.domain.Spot;

public class BarcodeRepository extends GenericRepository<Spot> {
	
	public BarcodeRepository() {
		super();
		super.clazz = Spot.class;
	}

}
