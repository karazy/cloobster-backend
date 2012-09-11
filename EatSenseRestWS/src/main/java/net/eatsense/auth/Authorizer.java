package net.eatsense.auth;

import java.security.Principal;
import java.util.Iterator;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

/**
 * <p>SecurityContext used to perform authorization checks.</p>
 */
public final class Authorizer implements SecurityContext {
	public static final String TOKEN_AUTH = "TOKEN";
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
    
    protected Authorizer(final AccountController accountController,final UriInfo uriInfo, final Account account, final AccessToken token) {
    	this.accountController = accountController;
    	this.uriInfo = uriInfo;
		this.checkIn = null;
    	if(token != null) {
    		this.authScheme = TOKEN_AUTH;
    	}
    	else {
    		this.authScheme = BASIC_AUTH;
    	}
    	this.token = token;
    	 
    	this.account = account;
		
		for (Iterator<PathSegment> iterator = uriInfo.getPathSegments(true).iterator(); iterator.hasNext();) {
			PathSegment pathSegment = iterator.next();
			if(pathSegment.getPath().equals("businesses") ) {
				
					if(iterator.hasNext()) {
						try {
							businessId = Long.valueOf(iterator.next().getPath());
							break;
						} catch (NumberFormatException e) {
							logger.info("Invalid businessId specified.");
						}
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
    	// guest role is for checkedin customers
    	if( role.equals(Role.GUEST) && ( ( checkIn != null && checkIn.getUserId() != null ) ||
    										account.getActiveCheckIn() != null ) )
    		return true;
    	
    	if( role.equals(Role.USER) && (accountController.isAccountInRole(account, role))) {
    		return true;
    	}
    	
    	if(accountController.isAccountInRole(account, role)) {
    		if( businessId == null || businessId == 0)
    			return true;
    		else 
    			return accountController.isAccountManagingBusiness(account, businessId);
    	}
    	
    	logger.warn("Account({}) not in role: {}", account.getId(), role);
    	
        return false;
    }

    public boolean isSecure() {
        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

    public String getAuthenticationScheme() {
        return this.authScheme;
    }
}