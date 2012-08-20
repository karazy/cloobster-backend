package net.eatsense.restws.customer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.NewCustomerAccountEvent;
import net.eatsense.representation.BusinessAccountDTO;
import net.eatsense.representation.CustomerAccountDTO;

@Path("/c/accounts")
@Produces("application/json; charset=utf-8")
public class AccountsResource {
	private final AccountController accountCtrl;
	
	@Context
	HttpServletRequest servletRequest;
	
	private final Provider<AccessTokenRepository> accessTokenRepoProvider;

	private final EventBus eventBus;

	@Inject
	public AccountsResource(AccountController accountCtrl, Provider<AccessTokenRepository> accessTokenRepoProvider, EventBus eventBus) {
		super();
		this.accountCtrl = accountCtrl;
		this.accessTokenRepoProvider = accessTokenRepoProvider;
		this.eventBus = eventBus;
	}
	
	@POST
	@Consumes("application/json; charset=utf-8")
	public CustomerAccountDTO createAccount(CustomerAccountDTO accountData, @Context UriInfo uriInfo) {
		Account account = accountCtrl.registerNewCustomerAccount(accountData);
		eventBus.post(new NewCustomerAccountEvent(account, uriInfo));
		return new CustomerAccountDTO(account);
	}
	
	@PUT
	@Path("confirmation/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	public CustomerAccountDTO confirmEmail(@PathParam("accessToken") String accessToken) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		Account account = accountCtrl.confirmAccountEmail(token.getAccount());
		accessTokenRepository.delete(token);
		
		eventBus.post(new ConfirmedAccountEvent(account));
		return new CustomerAccountDTO(account);
	}
}
