package net.eatsense.persistence;

import net.eatsense.domain.Bill;

public class BillRepository extends GenericRepository<Bill> {

	final static Class<Bill> entityClass = Bill.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	
	public BillRepository() {
		super(entityClass);
	}
}
