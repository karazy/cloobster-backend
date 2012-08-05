package net.eatsense.event;

import net.eatsense.domain.Account;

public class AccountEvent {
	private final Account account;
	
	public AccountEvent(Account account) {
		super();
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
}
