package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	//@RolesAllowed({"restaurantadmin", "user"})
	public AccountDTO getAccount(@PathParam("login") String login, @HeaderParam("password") String password) {
		Account account = null;
		AccountDTO accountData = null;
		if( (account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account")) != null ) {
			accountData = accountCtr.toDto(account);
		}
		else {
			accountData = accountCtr.getAccount(login, password);
			if(accountData == null )
				throw new WebApplicationException(401);
		}
		
		logger.info("Authenticated request from user :" + accountData.getLogin());
		accountCtr.getAccount(login, password);
		return accountData;
	}
	
	@GET
	@Path("{login}/businesses")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"user"})
	public Collection<BusinessDTO> getBusinessesForAccount(@PathParam("login") String login) {
		return accountCtr.getBusinessDtos(login);
	}
	
	@POST
	@Path("{login}/tokens")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/x-www-form-urlencoded; charset=UTF-8")
	@RolesAllowed({"restaurantadmin"})
	public String requestToken(@PathParam("login") String login, @FormParam("businessId") Long businessId, @FormParam("clientId") String clientId) {
		return accountCtr.requestToken(businessId, clientId);
	};
	
}
