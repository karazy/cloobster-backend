/**
 * 
 */
package net.eatsense.auth;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.AccountController;
import net.eatsense.controller.CheckInController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.CheckInRepository;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * @author Nils Weiher
 *
 */
public class SecurityFilter implements ContainerRequestFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	private CheckInController checkInCtrl;
	
	@Inject
	private AccountController accountCtrl;
	
    public void setAccountCtrl(AccountController accountCtrl) {
		this.accountCtrl = accountCtrl;
	}

	/**
     * <p>The URI information for this request.</p>
     */
    @Context
    UriInfo uriInfo;
    
    @Context
    HttpServletRequest servletRequest;
	
	public void setCheckInCtr(CheckInController checkInCtr) {
		this.checkInCtrl = checkInCtr;
	}

	/* (non-Javadoc)
	 * @see com.sun.jersey.spi.container.ContainerRequestFilter#filter(com.sun.jersey.spi.container.ContainerRequest)
	 */
	@Override
	public ContainerRequest filter(ContainerRequest request) {
		String checkInId = request.getQueryParameters(true).getFirst("checkInId");
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
			if(password != null && !password.isEmpty()) {
				// Authenticate with the clear password, should usually been doing only once during a session.
				account = accountCtrl.authenticate(login, password);
				
				if(account != null) {
					request.setSecurityContext(new Authorizer(account));
					servletRequest.setAttribute("net.eatsense.domain.Account", account);
					logger.info("authentication success for user: "+login);
					return request;
				}
				else {
					logger.info("Failed login for user: "+login);
				}
					
			}
			if(passwordHash != null && !passwordHash.isEmpty()) {
				// Authenticate with hash comparison ...
				account = accountCtrl.authenticateHashed(login, passwordHash);
				
				if(account != null) {
					request.setSecurityContext(new Authorizer(account));
					servletRequest.setAttribute("net.eatsense.domain.Account", account);
					logger.info("authentication success for user: "+login);
					return request;
				}	
			}
			
			
		}
		return request;
	}

	private Authorizer authenticateCheckIn(String checkInId) {
		CheckIn checkIn = null;
		try {
			checkIn = checkInCtrl.getCheckIn(checkInId);
		} catch (NotFoundException e) {
			return null;
		}
		logger.info("Valid user request recieved from " + checkIn.getNickname());
		// return a security context build around the checkIn
		return new Authorizer(checkIn);
	}
	
	/**
     * <p>SecurityContext used to perform authorization checks.</p>
     */
    public class Authorizer implements SecurityContext {
    	
    	private CheckIn checkIn;
		private Account account;

        public Authorizer(final CheckIn checkIn) {
        	this.checkIn = checkIn;
            this.principal = new Principal() {
                public String getName() {
                		return checkIn.getUserId();
                }
            };
        }
        
        public Authorizer(final Account account) {
        	this.account = account;
            this.principal = new Principal() {
                public String getName() {
                		return account.getLogin();
                }
            };
        }

        private Principal principal;

        public Principal getUserPrincipal() {
        	
            return this.principal;
        }

        /**
         * <p>Determine whether the authenticated user possesses the requested
         * role.</p>
         * @param role Role to be checked
         */
        public boolean isUserInRole(String role) {
        	if( role.equals("guest") && checkIn != null && checkIn.getUserId() != null)
        		return true;
        	

        	if(account != null && role.equals(account.getRole()))
        		return true;
		
            return false;
        }

        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        public String getAuthenticationScheme() {
            return SecurityContext.FORM_AUTH;
        }
    }

}
