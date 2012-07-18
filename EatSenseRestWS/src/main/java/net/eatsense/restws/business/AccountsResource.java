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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.controller.MailController;
import net.eatsense.domain.Account;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.EmailConfirmationDTO;
import net.eatsense.representation.RegistrationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Path("b/accounts")
public class AccountsResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	private MailController mailCtrl;
	
	@Inject
	public AccountsResource(AccountController accountCtr, MailController mailCtr) {
		super();
		this.mailCtrl = mailCtr;
		this.accountCtr = accountCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RegistrationDTO registerAccount(RegistrationDTO accountData, @Context UriInfo uriInfo) {
		Account account = accountCtr.registerNewAccount(accountData);
		try {
			String unsubcribeUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/account/confirm/{token}").build(account.getEmailConfirmationHash()).toString();
			mailCtrl.sendRegistrationConfirmation(unsubcribeUrl, account);
		} catch (AddressException e) {
			logger.error("sending confirmation mail failed", e);
			if(account.getEmail().equals(e.getRef())) {
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
	@Path("emailconfirmation")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public EmailConfirmationDTO confirmEmail(EmailConfirmationDTO emailData) {
		return accountCtr.confirmAccountEmail(emailData);
	}
}
