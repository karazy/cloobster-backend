package net.eatsense.restws;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.eatsense.controller.AccountController;
import net.eatsense.representation.AccountDTO;

import com.google.inject.Inject;

@Path("accounts")
public class AccountResource {
	private AccountController accountCtr;

	@Inject
	public AccountResource(AccountController accountCtr) {
		super();
		this.accountCtr = accountCtr;
	}
	@GET
	@Path("{login}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"restaurantadmin"})
	public AccountDTO getAccount(@PathParam("login") String login, @HeaderParam("password") String password) {
		return accountCtr.getAccount(login, password);
	}
	
}
