package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Authorizer;
import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.NewAccountEvent;
import net.eatsense.event.UpdateAccountEmailEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.CustomerAccountDTO;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Path("/c/accounts")
@Produces("application/json; charset=utf-8")
public class AccountsResource {
	private final AccountController accountCtrl;
	
	@Context
	HttpServletRequest servletRequest;
	
	private final Provider<AccessTokenRepository> accessTokenRepoProvider;

	private final EventBus eventBus;
	
	@Context
	private SecurityContext securityContext;

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
		eventBus.post(new NewAccountEvent(account, uriInfo));
		return new CustomerAccountDTO(account);
	}
	
	/**
	 * @param accountId
	 * @param accountData
	 * @return
	 */
	@PUT
	@Path("{accountId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.USER})
	public CustomerAccountDTO updateAccountProfile(@PathParam("accountId") Long accountId, CustomerAccountDTO accountData, @Context UriInfo uriInfo) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		if(!account.getId().equals(accountId)) {
			throw new IllegalAccessException("Can only update the authenticated account.");
		}
		
		if(!Strings.isNullOrEmpty(accountData.getPassword())) {
			if(securityContext.getAuthenticationScheme().equals(Authorizer.TOKEN_AUTH)) {
				throw new IllegalAccessException("Must authenticate with user credentials to change password.");
			}
		}
		String previousNewEmail = account.getNewEmail();
		
		Account updateAccount = accountCtrl.updateAccount(account, accountData);
		
		if(account.getNewEmail() != null && !Objects.equal(previousNewEmail, account.getNewEmail()) ) {
			eventBus.post(new UpdateAccountEmailEvent(account, uriInfo));
		}
		return new CustomerAccountDTO(updateAccount);
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
