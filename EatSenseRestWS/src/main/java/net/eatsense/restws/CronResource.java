package net.eatsense.restws;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;

@Path("/cron")
public class CronResource {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AccessTokenRepository accessTokenRepo;

	@Inject
	public CronResource(AccessTokenRepository accessTokenRepo) {
		this.accessTokenRepo = accessTokenRepo;
	}
	
	/**
	 * Delete all expired access tokens from the datastore.
	 * 
	 * @return "OK" if done.
	 */
	@GET
	@Path("cleantokens")
	public String deleteExpiredTokens() {
		QueryResultIterable<Key<AccessToken>> expiredTokens = this.accessTokenRepo.query().filter("expires <=", new Date()).fetchKeys();
		
		accessTokenRepo.delete(expiredTokens);
		
		return "OK";
	}
}
