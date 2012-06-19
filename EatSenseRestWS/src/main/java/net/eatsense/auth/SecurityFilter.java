/**
 * 
 */
package net.eatsense.auth;

import java.security.Principal;
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
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * @author Nils Weiher
 *
 */
public class SecurityFilter implements ContainerRequestFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private AccountController accountCtrl;
	
	/**
     * <p>The URI information for this request.</p>
     */
    @Context
    UriInfo uriInfo;
    
    @Context
    HttpServletRequest servletRequest;
    
	private CheckInRepository checkInRepo;
	
	@Inject
	public SecurityFilter(CheckInRepository checkInRepo, AccountController accountController) {
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
		
		String checkInId = request.getHeaderValue("checkInId");
		if(checkInId == null)
			checkInId = request.getQueryParameters(true).getFirst("checkInId");
					
		String login = request.getHeaderValue("login");
		String password = request.getHeaderValue("password");
		String passwordHash = request.getHeaderValue("passwordHash");
		
		Long businessId = null;
		
		for (Iterator<PathSegment> iterator = request.getPathSegments(true).iterator(); iterator.hasNext();) {
			PathSegment pathSegment = iterator.next();
			if(pathSegment.getPath().equals("businesses") ) {
				try {
					if(iterator.hasNext())
						businessId = Long.valueOf(iterator.next().getPath());
				} catch (NumberFormatException e) {
				}
			}
		}
		
		if(businessId == null) {
			if( request.getFormParameters().getFirst("businessId") != null)
				try {
					businessId = Long.valueOf(request.getFormParameters().getFirst("businessId"));
				} catch (NumberFormatException e) {
					logger.error("businessId invalid");
				}
				
		}
		
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
				
				if(account != null) {
					request.setSecurityContext(new Authorizer(account, businessId));
					servletRequest.setAttribute("net.eatsense.domain.Account", account);
					logger.info("authentication success for user: "+login);
					return request;
				}	
			}
			
			if(password != null && !password.isEmpty()) {
				// Authenticate with hash comparison ...
				account = accountCtrl.authenticate(login, password);
				
				if(account != null) {
					request.setSecurityContext(new Authorizer(account, businessId));
					servletRequest.setAttribute("net.eatsense.domain.Account", account);
					logger.info("authentication success for user: "+login);
					return request;
				}	
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
			return new Authorizer(checkIn);
		}
	}
	
	/**
     * <p>SecurityContext used to perform authorization checks.</p>
     */
    public class Authorizer implements SecurityContext {
    	
    	private final CheckIn checkIn;
		private final Account account;
		private final Long businessId;

        public Authorizer(final CheckIn checkIn) {
        	this.account = null;
        	this.businessId = null;
        	this.checkIn = checkIn;
            this.principal = new Principal() {
                public String getName() {
                		return checkIn.getUserId();
                }
            };
        }
        
        public Authorizer(final Account account, Long businessId) {
        	this.checkIn = null;
        	this.account = account;
        	this.businessId = businessId;
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
        	// guest role is for checkedin customers
        	if( role.equals(Role.GUEST) && checkIn != null && checkIn.getUserId() != null)
        		return true;
       	
        	if(accountCtrl.isAccountInRole(account, role)) {
        		if( businessId == null || businessId == 0)
        			return true;
        		else 
        			return accountCtrl.isAccountManagingBusiness(account, businessId);
        	}
        	
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
