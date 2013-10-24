package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.Account;

public class NewAccountEvent extends AccountEvent {
	private final UriInfo uriInfo;
	private final String whitelabel;
	
	public NewAccountEvent(Account account, UriInfo uriInfo, String whitelabel) {
		super(account);
		this.uriInfo = uriInfo;
		this.whitelabel = whitelabel;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	/**
	 * @return the whitelabel
	 */
	public String getWhitelabel() {
		return whitelabel;
	}
	
	
}
