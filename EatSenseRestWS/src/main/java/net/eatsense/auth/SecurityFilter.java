/**
 * 
 */
package net.eatsense.auth;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.CheckInRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		if(checkInId != null && !checkInId.isEmpty()) {
			 Authorizer auth = authenticateCheckIn(checkInId);
			 if(auth != null) {
				 request.setSecurityContext(auth);
				 return request;
			 }
		}
				
		if(login != null && !login.isEmpty()) {
			logger.info("recieved login request from user: " +login);
			Account account = null;

			if(passwordHash != null && !passwordHash.isEmpty()) {
				// Authenticate with hash comparison ...
				account = accountCtrl.authenticateHashed(login, passwordHash);
			}
			
			if(password != null && !password.isEmpty()) {
				account = accountCtrl.authenticate(login, password);
			}

			if(account != null) {
				request.setSecurityContext(authorizerFactory.createForAccount(account, null));
				servletRequest.setAttribute("net.eatsense.domain.Account", account);
				logger.info("authentication success for user: "+login);
				return request;
			}			
		}
		return request;
	}

	private Authorizer authenticateCheckIn(String checkInId) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.info("Invalid checkin uid given {}", checkInId);
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
