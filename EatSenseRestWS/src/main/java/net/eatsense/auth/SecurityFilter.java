/**
 * 
 */
package net.eatsense.auth;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.service.FacebookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * @author Nils Weiher
 *
 */
public class SecurityFilter implements ContainerRequestFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	AccountController accountCtrl;
	
	/**
     * <p>The URI information for this request.</p>
     */
    @Context
    UriInfo uriInfo;
    
    @Context
    HttpServletRequest servletRequest;
    
    private @Context SecurityContext securityContext;
    
	private final CheckInRepository checkInRepo;
	
	private final AuthorizerFactory authorizerFactory;
	
	@Inject
	public SecurityFilter(Injector injector, CheckInRepository checkInRepo, AccountController accountController, AuthorizerFactory authorizerFactory) {
		this.authorizerFactory = authorizerFactory;
		this.checkInRepo = checkInRepo;
		this.accountCtrl = accountController;
	}

	/* (non-Javadoc)
	 * @see com.sun.jersey.spi.container.ContainerRequestFilter#filter(com.sun.jersey.spi.container.ContainerRequest)
	 */
	@Override
	public ContainerRequest filter(ContainerRequest request) {
		// If we receive an OPTIONS request, do nothing.
		if(request.getMethod().equals("OPTIONS"))
			return request;
		
		if(securityContext.getAuthenticationScheme() == Authorizer.TOKEN_AUTH) {
			// Request was already authenticated with an access token, do nothing.
			return request;
		}
			
		String checkInId = request.getHeaderValue("checkInId");
		if(checkInId == null)
			checkInId = request.getQueryParameters(true).getFirst("checkInId");
					
		String login = request.getHeaderValue("login");
		String password = request.getHeaderValue("password");
		String passwordHash = request.getHeaderValue("passwordHash");
		String fbUserId = servletRequest.getHeader(FacebookService.FB_USERID_HEADER);
		String fbAccessToken = servletRequest.getHeader(FacebookService.FB_ACCESSTOKEN_HEADER);
		Account account = null;
		
		if(login != null && !login.isEmpty()) {
			logger.info("recieved login request from user: " +login);
			if(passwordHash != null && !passwordHash.isEmpty()) {
				// Authenticate with hash comparison ...
				account = accountCtrl.authenticateHashed(login, passwordHash);
			}
			
			if(password != null && !password.isEmpty()) {
				account = accountCtrl.authenticate(login, password);
			}
		
			if(account != null) {
				request.setSecurityContext(authorizerFactory.createForAccount(account, null,null));
				logger.info("Basic authentication success for user: {}", login);
			}
		}
		else if(!Strings.isNullOrEmpty(fbUserId) && !Strings.isNullOrEmpty(fbAccessToken)) {
			logger.info("Received Facebook authentication request for fb id: {}", fbUserId);
			// Authenticate via Facebook servers.
			account = accountCtrl.authenticateFacebook(fbUserId, fbAccessToken);
			request.setSecurityContext(authorizerFactory.createForAccount(account, null, Authorizer.FB_AUTH));
			
			logger.info("Facebook authentication success for user: {}", account.getEmail());
		}
		else if(!Strings.isNullOrEmpty(checkInId)) {
			// Set authorization for GUEST role, no account data was supplied.
			Authorizer auth = authenticateCheckIn(checkInId);
			if(auth != null) {
				request.setSecurityContext(auth);
				return request;
			}
		}
		
		servletRequest.setAttribute("net.eatsense.domain.Account", account);
		
		if( account != null) {
			if(account.getActiveCheckIn() != null) {
				try {
					servletRequest.setAttribute("net.eatsense.domain.CheckIn", checkInRepo.getByKey(account.getActiveCheckIn()));
				} catch (com.googlecode.objectify.NotFoundException e) {
					logger.info("activeCheckin for account not found, removing reference");
					accountCtrl.removeActiveCheckIn(account);
				}
			}
			else if(!Strings.isNullOrEmpty(checkInId)) {
				CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
				if(checkIn == null) {
					logger.warn("Invalid checkInId given {}", checkInId);
				}
				servletRequest.setAttribute("net.eatsense.domain.CheckIn", checkIn);
			}
		}
		
		return request;
	}

	private Authorizer authenticateCheckIn(String checkInId) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.warn("Invalid checkin uid given {}", checkInId);
			return null;
		}
		else {
			logger.info("Valid user request recieved from " + checkIn.getNickname());
			servletRequest.setAttribute("net.eatsense.domain.CheckIn", checkIn);
			// return a security context build around the checkIn
			return authorizerFactory.createForCheckIn(checkIn);
		}
	}
}
