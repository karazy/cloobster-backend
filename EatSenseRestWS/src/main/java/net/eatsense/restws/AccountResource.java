package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import com.google.inject.Inject;

@Path("accounts")
public class AccountResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	
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
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		if(account == null)
			throw new WebApplicationException(401);
		logger.info("Authenticated request from user :" + ((Account)servletRequest.getAttribute("net.eatsense.domain.Account")).getLogin());
		return accountCtr.toDto(account);
	}
	
	@GET
	@Path("{login}/businesses")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"restaurantadmin"})
	public Collection<BusinessDTO> getBusinessesForAccount(@PathParam("login") String login) {
		return accountCtr.getBusinessDtos(login);
	}
	
}