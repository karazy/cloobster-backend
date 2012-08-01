package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.Authorizer;
import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.controller.ChannelController;
import net.eatsense.domain.Account;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

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
	
	/**
	 * @param clientId
	 * @param businessId
	 * @return
	 */
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
	@RolesAllowed({Role.USER, Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public AccountDTO getAccount(@PathParam("login") String login, @HeaderParam("password") String password) {
		return this.getAccount();
	}
	
	@GET
	@Path("login")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.USER, Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public AccountDTO getAccount() {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		AccessToken token = (AccessToken)servletRequest.getAttribute(AccessToken.class.getName());
		
		AccountDTO accountDTO = new AccountDTO(account);
		accountDTO.setAccessToken(token != null ? token.getToken() : null);
		return accountDTO;
	}
	
	@POST
	@Path("tokens")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.USER, Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public AccountDTO createToken() {
		if(servletRequest.getAuthType() == Authorizer.TOKEN_AUTH) {
			throw new IllegalAccessException("Must re-authenticate with user credentials.");
		}
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		AccountDTO accountDto = new AccountDTO(account);
		AccessToken authToken = accountCtr.createAuthenticationToken(account);
		logger.info("Token created, expires on {}", authToken.getExpires());
		accountDto.setAccessToken(authToken.getToken());
		
		return accountDto;
	}
	
	@POST
	@Path("password-reset")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public String createPasswordReset(AccountDTO accountData,  @Context UriInfo uriInfo) {
		accountCtr.createAndSendPasswordResetToken(accountData.getEmail(), uriInfo);
		return "OK";
	}
	
	@GET
	@Path("loginfb")
	@Produces("application/json; charset=UTF-8")
	public AccountDTO getAccountFacebook(@QueryParam("uid") String uid, @QueryParam("token") String accessToken) {
		Account account = accountCtr.authenticateFacebook(uid, accessToken);
		logger.info("Authenticated request from user :" + account.getLogin());
		return new AccountDTO(account);
	}
	
	@GET
	@Path("{login}/businesses")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.USER, Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Collection<BusinessDTO> getBusinessesForAccount(@PathParam("login") String login) {
		//TODO Refactor call to getBusinessesForAccount
		return accountCtr.getBusinessDtos(login);
	}
}
