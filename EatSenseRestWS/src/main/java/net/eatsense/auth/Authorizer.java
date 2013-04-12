package net.eatsense.auth;

import java.security.Principal;
import java.util.Iterator;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

/**
 * <p>SecurityContext used to perform authorization checks.</p>
 */
public final class Authorizer implements SecurityContext {
	public static final String TOKEN_AUTH = "TOKEN";
	public static final String FB_AUTH = "FACEBOOK";
	private final String authScheme;
	private final CheckIn checkIn;
	private final Account account;
	private Long businessId;
	private final AccountController accountController;
	private final UriInfo uriInfo;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AccessToken token;

    protected Authorizer(AccountController accountController, UriInfo uriInfo, final CheckIn checkIn) {
    	this.accountController = accountController;
    	this.uriInfo = uriInfo;
		this.authScheme = FORM_AUTH;
    	this.token = null;
    	this.account = null;
    	this.businessId = null;
    	this.checkIn = checkIn;
        this.principal = new Principal() {
            public String getName() {
            		return checkIn.getUserId();
            }
        };
    }
    
    protected Authorizer(final AccountController accountController,final UriInfo uriInfo, final Account account, final AccessToken token, final String authScheme) {
    	this.accountController = accountController;
    	this.uriInfo = uriInfo;
		this.checkIn = null;
    	if(token != null) {
    		this.authScheme = TOKEN_AUTH;
    	}
    	else {
    		this.authScheme = Strings.isNullOrEmpty(authScheme) ? BASIC_AUTH : authScheme;
    	}
    	this.token = token;
    	 
    	this.account = account;
    	
		for (Iterator<PathSegment> iterator = uriInfo.getPathSegments(true).iterator(); iterator.hasNext();) {
			PathSegment pathSegment = iterator.next();
			if(pathSegment.getPath().equals("b") && iterator.hasNext()) {
				pathSegment = iterator.next();
			}
			if(pathSegment.getPath().equals("businesses") && iterator.hasNext()) {
				try {
					businessId = Long.valueOf(iterator.next().getPath());
					break;
				} catch (NumberFormatException e) {
					logger.warn("Could not parse businessId from path");
				}
			}
		}
    	
        this.principal = new Principal() {
            public String getName() {
            	if(account.getLogin() != null)
            		return account.getLogin();
            	else
            		return account.getEmail();
            }
        };
    }

    private final Principal principal;

    public Principal getUserPrincipal() {
    	
        return this.principal;
    }

    /**
     * <p>Determine whether the authenticated user possesses the requested
     * role.</p>
     * @param role Role to be checked
     */
    public boolean isUserInRole(String role) {
    	// Check for "guest" role, requires an active checkin (either anonymous or authenticated)
    	if( role.equals(Role.GUEST) && ( isValidCheckIn() || isActiveAccountWithActiveCheckIn() ) )
    		return true;
    	
    	// Check for "user" role, requires an active account.
    	if( role.equals(Role.USER) && (accountController.isAccountInRole(account, role))) {
    		return true;
    	}
    	
		// Check for all other roles( "cockpituser", "businessadmin" and
		// "companyowner").
		// We include the check for businessId here because access to a business
		// requires further permission.
    	if(accountController.isAccountInRole(account, role)) {
    		if( businessId == null || businessId == 0)
    			return true;
    		else 
    			return accountController.isAccountManagingBusiness(account, businessId);
    	}
    	
        return false;
    }

	/**
	 * @return
	 */
	private boolean isActiveAccountWithActiveCheckIn() {
		return account != null && account.isActive() && account.getActiveCheckIn() != null;
	}

	/**
	 * @return
	 */
	private boolean isValidCheckIn() {
		return checkIn != null && checkIn.getUserId() != null && !checkIn.isArchived();
	}

    public boolean isSecure() {
        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

    public String getAuthenticationScheme() {
        return this.authScheme;
    }
}