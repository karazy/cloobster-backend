package net.eatsense.persistence;

import net.eatsense.domain.Bill;

public class BillRepository extends GenericRepository<Bill> {

	final static Class<Bill> entityClass = Bill.class;
	
	public BillRepository() {
		super(entityClass);
	}
}
