package net.eatsense.event;

import net.eatsense.domain.Account;

public class ConfirmedAccountEvent extends AccountEvent {
	public ConfirmedAccountEvent(Account account) {
		super(account);
	}
}
