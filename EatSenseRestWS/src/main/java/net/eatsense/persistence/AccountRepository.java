package net.eatsense.persistence;

import net.eatsense.domain.Account;

public class AccountRepository extends GenericRepository<Account> {

	public AccountRepository() {
		super();
		super.clazz = Account.class;
	}
}
