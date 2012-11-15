package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.Account;

public class NewCompanyAccountEvent extends AccountEvent {
	private final UriInfo uriInfo;	
	private final Account ownerAccount;

	public NewCompanyAccountEvent(Account account, UriInfo uriInfo, Account ownerAccount) {
		super(account);
		this.ownerAccount = ownerAccount;
		this.uriInfo = uriInfo;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public Account getOwnerAccount() {
		return ownerAccount;
	}

}
