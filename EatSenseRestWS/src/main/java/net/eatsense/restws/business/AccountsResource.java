package net.eatsense.restws.business;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.eatsense.controller.AccountController;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.RegistrationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Path("b/accounts")
public class AccountsResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	public AccountsResource(AccountController accountCtr) {
		super();
		this.accountCtr = accountCtr;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public RegistrationDTO registerAccount(RegistrationDTO accountData) {
		return accountCtr.registerNewAccount(accountData);
	}
}
