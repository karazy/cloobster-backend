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
import net.eatsense.persistence.CheckInRepository;

import com.google.common.base.Objects;
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
	private static final String BUSINESS_PATH_PREFIX = "b";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String TOKEN_HEADER = "X-Auth";
	private final AccessTokenRepository accessTokenRepo;
	private final AccountRepository accountRepo;
	
	@Context
	ResourceContext resourceContext;
	
    @Context
    HttpServletRequest servletRequest;

	private final AuthorizerFactory authorizerFactory;

	private final CheckInRepository checkInRepo;
	
	@Inject
	public AccessTokenFilter(AccessTokenRepository accessTokenRepo, AccountRepository accountRepo, AuthorizerFactory authorizerFactory, CheckInRepository checkInRepo) {
		super();
		this.authorizerFactory = authorizerFactory;
		this.accountRepo = accountRepo;
		this.accessTokenRepo = accessTokenRepo;
		this.checkInRepo = checkInRepo;
	}

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		String stringToken = request.getHeaderValue(TOKEN_HEADER);
		//AbstractResourceMethod method = resourceContext.matchUriInfo(request.getRequestUri()).getMatchedMethod();
		//TokenType requiredToken = null;
		
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
				logger.warn("Access token no longer valid.");
				throw new IllegalAccessException("Access token invalid, please re-authenticate.");
			}
			
			if( accessToken.getExpires() != null &&  accessToken.getExpires().before(new Date()) ) {
				accessTokenRepo.delete(accessToken);
				logger.warn("Deleted expired access token for account {}", accessToken.getAccount());
				throw new IllegalAccessException("Access token exired, please re-authenticate.");	
			}
			
			if( accessToken.getType() == TokenType.AUTHENTICATION_CUSTOMER) {
				if(request.getPathSegments().get(0).equals(BUSINESS_PATH_PREFIX)) {
					logger.warn("Request for business resource with customer access token.");
					throw new IllegalAccessException("Can not access business resource with this access token.");
				}
			}
			
			
			Account account;
			try {
				account = accountRepo.getByKey(accessToken.getAccount());
			} catch (com.googlecode.objectify.NotFoundException e1) {
				accessTokenRepo.delete(accessToken);
				logger.warn("Deleted invalid access token, account no longer exists {}", accessToken.getAccount());
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
			servletRequest.setAttribute(AccessToken.class.getName(), accessToken);
			
			if(accessToken.getType() == TokenType.AUTHENTICATION || accessToken.getType() == TokenType.AUTHENTICATION_CUSTOMER) {
				request.setSecurityContext(authorizerFactory.createForAccount(account, accessToken, null));
				servletRequest.setAttribute("net.eatsense.domain.Account", account);
				if(account.getActiveCheckIn() != null) {
					try {
						servletRequest.setAttribute("net.eatsense.domain.CheckIn", checkInRepo.getByKey(account.getActiveCheckIn()));
					} catch (com.googlecode.objectify.NotFoundException e) {
						logger.info("activeCheckin for account not found, removing reference");
						account.setActiveCheckIn(null);
						accountRepo.saveOrUpdate(account);
					}
				}
				logger.info("Request authenticated for Account({}), login/email={}", account.getId(), Objects.firstNonNull(account.getLogin(), account.getEmail()));
			}
		}
//		else if(requiredToken != null) {
//			logger.info("No token supplied, but method requires access token of type: {}", requiredToken);
//			throw new IllegalAccessException("Access denied, access token required for this request.");
//		}
//		
		return request;
	}

}
