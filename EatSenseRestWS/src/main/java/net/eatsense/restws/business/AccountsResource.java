package net.eatsense.restws.business;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import net.eatsense.controller.AccountController;
import net.eatsense.controller.MailController;
import net.eatsense.domain.Account;
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
	public RegistrationDTO registerAccount(RegistrationDTO accountData) {
		Account account = accountCtr.registerNewAccount(accountData);
		try {
			mailCtrl.sendRegistrationConfirmation(account);
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
	
	@PUT
	@Path("emailconfirmation")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public EmailConfirmationDTO confirmEmail(EmailConfirmationDTO emailData) {
		return accountCtr.confirmAccountEmail(emailData);
	}
}
