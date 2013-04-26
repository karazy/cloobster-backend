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
		CheckIn checkIn = null;
		
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
		
		if( account != null) {
			// Authorize the account and load the associated checkIn
			servletRequest.setAttribute("net.eatsense.domain.Account", account);
			
			if(account.getActiveCheckIn() != null) {
				try {
					checkIn = checkInRepo.getByKey(account.getActiveCheckIn());
					
					// We check here, if the activeCheckIn of the account is
					// different to the supplied checkIn from the header
					// parameter.
					if (!Strings.isNullOrEmpty(checkInId) && !checkInId.equals(checkIn.getUserId())) {
						// If it is different, there was an older checkIn from the account not completed.
						// Override the CheckIn for this request with the new checkin.
						checkIn = checkInRepo.getByProperty("userId", checkInId);
					}
				} catch (com.googlecode.objectify.NotFoundException e) {
					logger.warn("activeCheckin for account not found, removing reference");
					accountCtrl.removeActiveCheckIn(account);
				}
			}
			else if (!Strings.isNullOrEmpty(checkInId)) {
				checkIn = checkInRepo.getByProperty("userId", checkInId);								
			}
		}
		else if(!Strings.isNullOrEmpty(checkInId)) {
			// No account data supplied, authorize anonymous request via checkIn
			checkIn = checkInRepo.getByProperty("userId", checkInId);
			
			if(checkIn != null && !checkIn.isArchived()) {
				// Set authorization for GUEST role, no account data was supplied.
				logger.info("Valid guest request recieved from " + checkIn.getNickname());
				Authorizer auth = authorizerFactory.createForCheckIn(checkIn);
				if(auth != null) {
					request.setSecurityContext(auth);
				}
			}
		}
		
		if(checkIn == null) {
			if(!Strings.isNullOrEmpty(checkInId))
				logger.warn("Invalid checkInId given {}", checkInId);
		}
		else if(checkIn.isArchived()) {
			logger.warn("CheckIn already archived, unauthorized access. id={}", checkIn.getId());
		}
		else {
			servletRequest.setAttribute("net.eatsense.domain.CheckIn", checkIn);
		}
		
		return request;
	}
}
