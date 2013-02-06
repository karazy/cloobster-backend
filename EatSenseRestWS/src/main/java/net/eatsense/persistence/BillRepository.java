package net.eatsense.persistence;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class BillRepository extends GenericRepository<Bill> {

	final static Class<Bill> entityClass = Bill.class;
	
	public BillRepository() {
		super(entityClass);
	}
	
	public Bill belongingToCheckInAndLocation(Business location, long checkInId) {
		return query().ancestor(location).filter("checkIn", CheckIn.getKey(checkInId)).get();
	}
}
