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
import javax.ws.rs.core.Context;

import net.eatsense.controller.AccountController;
import net.eatsense.controller.ChannelController;
import net.eatsense.domain.Account;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.NotFoundException;

@Path("accounts")
public class AccountResource {
	private AccountController accountCtr;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	private final Provider<ChannelController> channelCtrlProvider;
	
	@Inject
	public AccountResource(AccountController accountCtr, Provider<ChannelController> channelCtrlProvider) {
		super();
		this.channelCtrlProvider = channelCtrlProvider;
		this.accountCtr = accountCtr;
	}
	
	@GET
	@Path("channels")
	@Produces("text/plain; charset=UTF-8")
	public String checkOnlineStatus(@QueryParam("clientId")String clientId, @QueryParam("businessId") Long businessId) {
		ChannelController channelController = channelCtrlProvider.get();
		return channelController.checkOnlineStatus(businessId, clientId);
	}
	
	@GET
	@Path("{login}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"user"})
	public AccountDTO getAccount(@PathParam("login") String login, @HeaderParam("password") String password) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		logger.info("Authenticated request from user :" + account.getLogin());
		return accountCtr.toDto(account);
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
	public String requestToken(@PathParam("login") String login, @FormParam("businessId") long businessId, @FormParam("clientId") String clientId) {
		//Set the timeout to 240 minutes (4 hours)
		String token = channelCtrlProvider.get().createCockpitChannel(businessId, clientId, Optional.of(240));
		if(token == null)
			throw new NotFoundException();
		return token;
	};
	
	
	
}
