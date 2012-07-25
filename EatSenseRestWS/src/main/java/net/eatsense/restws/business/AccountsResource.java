package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
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
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Role;
import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.controller.AccountController;
import net.eatsense.controller.MailController;
import net.eatsense.domain.Account;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.CompanyAccountDTO;
import net.eatsense.representation.EmailConfirmationDTO;
import net.eatsense.representation.RegistrationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

@Path("b/accounts")
public class AccountsResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	private MailController mailCtrl;
	private final Provider<AccessTokenRepository> accessTokenRepoProvider;
	
	@Inject
	public AccountsResource(AccountController accountCtr, MailController mailCtr, Provider<AccessTokenRepository> accessTokenRepoProvider) {
		super();
		this.accessTokenRepoProvider = accessTokenRepoProvider;
		this.mailCtrl = mailCtr;
		this.accountCtr = accountCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RegistrationDTO registerAccount(RegistrationDTO accountData, @Context UriInfo uriInfo) {
		Account newAccount = accountCtr.registerNewAccount(accountData);
		String accessToken = accountCtr.createConfirmAccountToken(newAccount).getToken();
		try {
			String unsubcribeUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/account/confirm/{token}").build(accessToken).toString();
			mailCtrl.sendRegistrationConfirmation(unsubcribeUrl, newAccount);
		} catch (AddressException e) {
			logger.error("sending confirmation mail failed", e);
			if(newAccount.getEmail().equals(e.getRef())) {
				throw new IllegalArgumentException("invalid email", e);
			}
		} catch (MessagingException e) {
			logger.error("sending confirmation mail failed", e);
		}
		return accountData;
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
	public List<AccountDTO> getAccounts(@QueryParam("email") String email) {
		return accountCtr.getAccountsByEmail(email);
	}
	
	@PUT
	@Path("confirmation/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public AccountDTO confirmEmail(@PathParam("accessToken") String accessToken) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		AccountDTO accountDTO = new AccountDTO(accountCtr.confirmAccountEmail(token.getAccount()));
		
		accessTokenRepository.delete(token);
		return accountDTO;
	}
	
	@PUT
	@Path("setup/{accessToken}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public AccountDTO setupAccount(@PathParam("accessToken") String accessToken, CompanyAccountDTO accountData) {
		AccessTokenRepository accessTokenRepository = accessTokenRepoProvider.get();
		AccessToken token = accessTokenRepository.get(accessToken);
		
		if(token.getType() != TokenType.ACCOUNTSETUP) {
			throw new IllegalAccessException("Invalid token.");
		}
		AccountDTO accountDto = accountCtr.setupAdminAccount(token.getAccount(), accountData);
		accessTokenRepository.delete(token);
		return accountDto;
	}
}
