package net.eatsense.auth;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.domain.Account;
import net.eatsense.util.IdHelper;

import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

/**
 * @author Nils Weiher
 *
 */
@Singleton
public class AccessTokenRepository extends DAOBase {
	static {
		ObjectifyService.register(AccessToken.class);
	}
	
	/**
	 * @param accountKey
	 * @return
	 */
	public AccessToken createAuthToken(Key<Account> accountKey, boolean isPermanent) {
		Date expirationDate = null;
		
		if(!isPermanent) {
			Calendar cal = Calendar.getInstance();
			//TODO: Extract the number of hours an authentication token is valid to the web.xml config.
		
        	cal.add(Calendar.HOUR_OF_DAY, 120);
        	expirationDate = cal.getTime();
		}
		
		AccessToken token = new AccessToken(IdHelper.generateId(), TokenType.AUTHENTICATION, expirationDate, accountKey);
        ofy().put(token);
        return token;
	}
	
	/**
	 * @param type
	 * @param accountKey
	 * @param expires
	 * @return
	 */
	public AccessToken create(TokenType type, Key<Account> accountKey, Date expires) {
		AccessToken token = new AccessToken(IdHelper.generateId(), type, expires, accountKey);
		ofy().put(token);
		return token;
	}
	
	/**
	 * @param token
	 * @return
	 * @throws net.eatsense.exceptions.NotFoundException
	 */
	public AccessToken get(String token) throws net.eatsense.exceptions.NotFoundException {
		try {
			return ofy().get(AccessToken.class, token);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException("Unknown token supplied.", "error.accesstoken.invalid");
		}
	}
	
	/**
	 * @param accountKey
	 * @return All access tokens for the account.
	 */
	public List<AccessToken> getForAccount(Key<Account> accountKey) {
		return ofy().query(AccessToken.class).filter("account", accountKey).list();
	}
	
	/**
	 * @param accountKey
	 * @param type
	 * @return All access token keys for the type and account.
	 */
	public List<Key<AccessToken>> getKeysForAccountAndType(Key<Account> accountKey, TokenType type) {
		return ofy().query(AccessToken.class).filter("account", accountKey).filter("type", type).listKeys();
	}
	
	/**
	 * Delete tokens in a parallel operation.
	 * 
	 * @param tokensOrKeys
	 */
	public void delete(Iterable<?> tokensOrKeys) {
		ofy().delete(tokensOrKeys);
	}
	
	/**
	 * Delete token from store.
	 * 
	 * @param token
	 */
	public void delete(AccessToken token) {
		ofy().delete(token);
	}
	
	/**
	 * Start datatsore query.
	 * 
	 * @return Query object
	 */
	public Query<AccessToken> query() {
		return ofy().query(AccessToken.class);
	}
}
