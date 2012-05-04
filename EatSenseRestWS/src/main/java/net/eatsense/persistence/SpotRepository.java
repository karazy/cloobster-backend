package net.eatsense.persistence;

import net.eatsense.domain.Spot;

public class SpotRepository extends GenericRepository<Spot> {
	static {
		GenericRepository.register(Spot.class);
	}	
	public SpotRepository() {
		super(Spot.class);
	}

}
