package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import net.eatsense.controller.ProfileController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.NewAccountEvent;
import net.eatsense.event.UpdateAccountEmailEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.CustomerAccountDTO;
import net.eatsense.representation.CustomerProfileDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Path("/c/accounts")
@Produces("application/json; charset=utf-8")
public class AccountsResource {
	private final Provider<AccountController> accountCtrlProvider;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	
	private final Provider<AccessTokenRepository> accessTokenRepoProvider;

	private final EventBus eventBus;
	
	@Context
	private SecurityContext securityContext;

	private final Provider<ProfileController> profileCtrlProvider;

	@Inject
	public AccountsResource(Provider<AccountController> accountCtrlProvider,
			Provider<AccessTokenRepository> accessTokenRepoProvider,
			Provider<ProfileController> profileCtrlProvider,
			EventBus eventBus) {
		super();
		this.profileCtrlProvider = profileCtrlProvider;
		this.accountCtrlProvider = accountCtrlProvider;
		this.accessTokenRepoProvider = accessTokenRepoProvider;
		this.eventBus = eventBus;
	}

	@POST
	@Consumes("application/json; charset=utf-8")
	public CustomerAccountDTO createAccount(CustomerAccountDTO accountData, @Context UriInfo uriInfo) {
		AccountController accountCtrl = accountCtrlProvider.get();
		Account account = accountCtrl.registerNewCustomerAccount(accountData);
		eventBus.post(new NewAccountEvent(account, uriInfo));
		CustomerAccountDTO accountDto = new CustomerAccountDTO(account);
		
		AccessToken authToken = accountCtrl.createCustomerAuthToken(account);
		
		logger.info("Permanent customer Token created");
		accountDto.setAccessToken(authToken.getToken());

		
		return accountDto;
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
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerAccountDTO updateAccount(@PathParam("accountId") Long accountId, CustomerAccountDTO accountData, @Context UriInfo uriInfo) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		if(!account.getId().equals(accountId)) {
			throw new IllegalAccessException("Can only update the authenticated account.");
		}
		
		if(!Strings.isNullOrEmpty(accountData.getPassword())) {
			if(securityContext.getAuthenticationScheme().equals(Authorizer.TOKEN_AUTH)) {
				throw new IllegalAccessException("Must authenticate with user credentials to change password.");
			}
		}
		String previousNewEmail = account.getNewEmail();
		
		Account updateAccount = accountCtrlProvider.get().updateCustomerAccount(account, accountData);
		
		if(account.getNewEmail() != null && !Objects.equal(previousNewEmail, account.getNewEmail()) ) {
			eventBus.post(new UpdateAccountEmailEvent(account, uriInfo));
		}
		return new CustomerAccountDTO(updateAccount, checkIn);
	}
	
	@POST
	@Path("tokens")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerAccountDTO createToken() {
		if(servletRequest.getAuthType() == Authorizer.TOKEN_AUTH) {
			throw new IllegalAccessException("Must re-authenticate with user credentials.");
		}
		
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		CheckIn checkIn = (CheckIn)servletRequest.getAttribute("net.eatsense.domain.CheckIn");
		
		CustomerAccountDTO accountDto = new CustomerAccountDTO(account, checkIn);
		AccessToken authToken;
		
		AccountController accountCtrl = accountCtrlProvider.get();
		authToken = accountCtrl.createCustomerAuthToken(account);
		
		logger.info("Permanent customer Token created");
		accountDto.setAccessToken(authToken.getToken());
		
		return accountDto;
	}
	
	@PUT
	@Path("{accountId}/profile")
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerProfileDTO updateProfile(@PathParam("accountId") Long accountId, CustomerProfileDTO profileData) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		if(!account.getId().equals(accountId)) {
			throw new IllegalAccessException("Can only update the authenticated account's profile.");
		}
		
		return new CustomerProfileDTO( profileCtrlProvider.get().updateCustomerProfile(account.getCustomerProfile(), profileData));
	}
	
	@GET
	@Path("{accountId}")
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerAccountDTO getAccount(@PathParam("accountId") Long accountId) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		
		if(!account.getId().equals(accountId)) {
			throw new IllegalAccessException("Tried to load wrong account!");
		}
		
		return new CustomerAccountDTO(account, null);
	}
	
	@PUT
	@Path("confirmation/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	public CustomerAccountDTO confirmEmail(@PathParam("accessToken") String accessToken) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		Account account = accountCtrlProvider.get().confirmAccountEmail(token.getAccount());
		accessTokenRepository.delete(token);
		
		eventBus.post(new ConfirmedAccountEvent(account));
		return new CustomerAccountDTO(account);
	}
}
