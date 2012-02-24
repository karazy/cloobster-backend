package net.eatsense.persistence;

import net.eatsense.domain.Bill;

public class BillRepository extends GenericRepository<Bill> {

	public BillRepository() {
		super();
		super.clazz = Bill.class;
	}
}
