package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.Account;

public class NewAccountEvent extends AccountEvent {
	private final UriInfo uriInfo;
	
	public NewAccountEvent(Account account, UriInfo uriInfo) {
		super(account);
		this.uriInfo = uriInfo;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}
}
