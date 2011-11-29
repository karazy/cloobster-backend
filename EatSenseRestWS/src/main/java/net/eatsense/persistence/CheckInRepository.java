package net.eatsense.persistence;

import net.eatsense.domain.CheckIn;

public class CheckInRepository extends GenericRepository<CheckIn> {
	
	public CheckInRepository() {
		super();
		super.clazz = CheckIn.class;
	}

}
