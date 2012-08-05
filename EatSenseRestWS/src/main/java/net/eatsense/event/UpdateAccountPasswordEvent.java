package net.eatsense.event;

import net.eatsense.domain.Account;

public class UpdateAccountPasswordEvent extends AccountEvent {

	public UpdateAccountPasswordEvent(Account account) {
		super(account);
	}

}
