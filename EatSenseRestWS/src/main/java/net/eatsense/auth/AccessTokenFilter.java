package net.eatsense.auth;

import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.domain.Account;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.AccountRepository;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Checks for an accesstoken in the header.
 * 
 * @author Nils Weiher
 *
 */
public class AccessTokenFilter implements ContainerRequestFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String TOKEN_HEADER = "X-Auth";
	private final AccessTokenRepository accessTokenRepo;
	private final AccountRepository accountRepo;
	
	@Context
	ResourceContext resourceContext;
	
    @Context
    HttpServletRequest servletRequest;
	
	@Inject
	public AccessTokenFilter(AccessTokenRepository accessTokenRepo, AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
		this.accessTokenRepo = accessTokenRepo;
	}

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		String stringToken = request.getHeaderValue(TOKEN_HEADER);
		//AbstractResourceMethod method = resourceContext.matchUriInfo(request.getRequestUri()).getMatchedMethod();
		//TokenType requiredToken = null;
		
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
		
//		if(method != null) {
//			TokenRequired tr = method.getAnnotation(TokenRequired.class);
//			if(tr != null) {
//				requiredToken = tr.value();
//			}
//		}
//		
		if(!Strings.isNullOrEmpty(stringToken)) {
			final AccessToken accessToken;
			try {
				accessToken = accessTokenRepo.get(stringToken);
			} catch (NotFoundException e) {
				throw new IllegalAccessException("Access token invalid, please re-authenticate.");
			}
			
			if(accessToken.getExpires().after(new Date()) ) {
				throw new IllegalAccessException("Access token exired, please re-authenticate.");	
			}
			
			Account account = accountRepo.getByKey(accessToken.getAccount());
			
			if(account == null) {
				throw new IllegalAccessException("Access token invalid, account no longer exists.");	
			}
			
//			if(requiredToken != null) {
//				if ( accessToken.getType() != requiredToken) {
//					logger.info("Token {},type={} invalid for requested uri.", stringToken, accessToken.getType());
//					throw new IllegalAccessException("Access denied, access token not valid for this request.");
//				}
//				else {
//					servletRequest.setAttribute("net.eatsense.domain.Account", account);
//				}
//			}
//			
//TODO: find a way to use Authorizer for both filters.
//			if(accessToken.getType() == TokenType.AUTHENTICATION) {
//				request.setSecurityContext(new Authorizer(account, businessId, accessToken));
//				servletRequest.setAttribute("net.eatsense.domain.Account", account);
//				logger.info("Request authenticated success for user: "+account.getLogin());
//			}
		}
//		else if(requiredToken != null) {
//			logger.info("No token supplied, but method requires access token of type: {}", requiredToken);
//			throw new IllegalAccessException("Access denied, access token required for this request.");
//		}
//		
		return request;
	}

}
