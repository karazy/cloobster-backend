package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Authorizer;
import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.NewAccountEvent;
import net.eatsense.event.UpdateAccountEmailEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.BusinessAccountDTO;
import net.eatsense.representation.RegistrationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Path("b/accounts")
public class AccountsResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private HttpServletRequest servletRequest;
	private @Context SecurityContext securityContext;
	
	private final Provider<AccessTokenRepository> accessTokenRepoProvider;
	private final EventBus eventBus;
	@Context
	private UriInfo uriInfo;

	@Inject
	public AccountsResource(AccountController accountCtr, Provider<AccessTokenRepository> accessTokenRepoProvider, EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		this.accessTokenRepoProvider = accessTokenRepoProvider;
		this.accountCtr = accountCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RegistrationDTO registerAccount(RegistrationDTO accountData) {
		Account newAccount = accountCtr.registerNewCompanyAccount(accountData);
		eventBus.post(new NewAccountEvent(newAccount, uriInfo));
		return accountData;
	}
	
	/**
	 * @param accountId
	 * @return retrieve profile
	 */
	@GET
	@Path("{accountId}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public BusinessAccountDTO getAccountProfile(@PathParam("accountId") Long accountId) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		if(!account.getId().equals(accountId)) {
			throw new IllegalAccessException("Can only read own account.");
		}
		
		return new BusinessAccountDTO(account);
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
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public BusinessAccountDTO updateAccountProfile(@PathParam("accountId") Long accountId, BusinessAccountDTO accountData) {
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
		
		Account updateAccount = accountCtr.updateAccount(account, accountData);
		
		if(account.getNewEmail() != null && !Objects.equal(previousNewEmail, account.getNewEmail()) ) {
			eventBus.post(new UpdateAccountEmailEvent(account, uriInfo));
		}
		return new BusinessAccountDTO(updateAccount);
	}
	
	/**
	 * Retrieve Accounts 
	 * 
	 * @param email Must be specified.
	 * @return Accounts saved with the specified e-mail address.
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.COMPANYOWNER)
	public List<BusinessAccountDTO> getAccounts(@QueryParam("email") String email) {
		return accountCtr.getAccountsByEmail(email);
	}
	
	@PUT
	@Path("email-confirmation/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public BusinessAccountDTO confirmNewEmail(@PathParam("accessToken") String accessToken) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		Account account = accountCtr.confirmAccountEmail(token.getAccount());
		accessTokenRepository.delete(token);
		
		return new BusinessAccountDTO(account);
	}
	
	@PUT
	@Path("confirmation/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public BusinessAccountDTO confirmEmail(@PathParam("accessToken") String accessToken) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		Account account = accountCtr.confirmAccountEmail(token.getAccount());
		accessTokenRepository.delete(token);
		
		eventBus.post(new ConfirmedAccountEvent(account));
		return new BusinessAccountDTO(account);
	}
	
	@PUT
	@Path("setup/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public BusinessAccountDTO setupAccount(@PathParam("accessToken") String accessToken, BusinessAccountDTO accountData) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		if(token.getType() != TokenType.ACCOUNTSETUP) {
			throw new IllegalAccessException("Invalid token.");
		}
		BusinessAccountDTO accountDto = accountCtr.setupAdminAccount(token.getAccount(), accountData);
		accessTokenRepository.delete(token);
		return accountDto;
	}
}
