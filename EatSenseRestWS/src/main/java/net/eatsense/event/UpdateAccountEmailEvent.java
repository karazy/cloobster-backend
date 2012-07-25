package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.Account;

public class UpdateAccountEmailEvent extends AccountEvent {

	private final UriInfo uriInfo;

	public UpdateAccountEmailEvent(Account account, UriInfo uriInfo) {
		super(account);
		this.uriInfo = uriInfo;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

}
