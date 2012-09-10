package net.eatsense.persistence;

import net.eatsense.domain.CustomerProfile;

public class CustomerProfileRepository extends
		GenericRepository<CustomerProfile> {

	public CustomerProfileRepository() {
		super(CustomerProfile.class);
	}

}
