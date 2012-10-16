package net.eatsense.auth;

import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;
import com.google.inject.Provider;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

public class AuthorizerFactoryImpl implements AuthorizerFactory {
	private final Provider<AccountController> accountControllerProvider;
	private final Provider<UriInfo> uriInfoProvider;
	
	@Inject
	public AuthorizerFactoryImpl(
			Provider<AccountController> accountControllerProvider,
			Provider<UriInfo> uriInfoProvider) {
		super();
		this.accountControllerProvider = accountControllerProvider;
		this.uriInfoProvider = uriInfoProvider;
	}

	@Override
	public Authorizer createForCheckIn(final CheckIn checkIn) {
		return new Authorizer(accountControllerProvider.get(), uriInfoProvider.get(), checkIn);
	}

	@Override
	public Authorizer createForAccount(final Account account, final AccessToken token, final String authScheme) {
		return new Authorizer(accountControllerProvider.get(), uriInfoProvider.get(), account, token, authScheme);
	}

}
