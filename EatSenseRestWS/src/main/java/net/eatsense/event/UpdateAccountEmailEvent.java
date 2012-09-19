package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import com.google.common.base.Optional;

import net.eatsense.domain.Account;

public class UpdateAccountEmailEvent extends AccountEvent {

	private final UriInfo uriInfo;
	private final Optional<String> previousEmail;

	public UpdateAccountEmailEvent(Account account, UriInfo uriInfo) {
		super(account);
		this.uriInfo = uriInfo;
		
		this.previousEmail = Optional.absent();
	}
	
	public UpdateAccountEmailEvent(Account account, UriInfo uriInfo, String previousEmail) {
		super(account);
		this.uriInfo = uriInfo;
		
		this.previousEmail = Optional.fromNullable(previousEmail);
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public Optional<String> getPreviousEmail() {
		return previousEmail;
	}

}
