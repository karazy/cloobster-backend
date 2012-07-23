package net.eatsense.auth;

import java.util.Calendar;
import java.util.Date;

import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.domain.Account;
import net.eatsense.util.IdHelper;

import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

@Singleton
public class AccessTokenRepository extends DAOBase {
	static {
		ObjectifyService.register(AccessToken.class);
	}
	
	public AccessToken createAuthToken(Key<Account> accountKey) {
		Calendar cal = Calendar.getInstance();
		//TODO: Extract the number of hours an authentication token is valid to the web.xml config.
        cal.add(Calendar.HOUR_OF_DAY, 120);
        AccessToken token = new AccessToken(IdHelper.generateId(), TokenType.AUTHENTICATION, cal.getTime(), accountKey);
        ofy().put(token);
        return token;
	}
	
	public AccessToken create(TokenType type, Key<Account> accountKey, Date expires) {
		AccessToken token = new AccessToken(IdHelper.generateId(), type, expires, accountKey);
		ofy().put(token);
		return token;
	}
	
	public AccessToken get(String token) throws net.eatsense.exceptions.NotFoundException {
		try {
			return ofy().get(AccessToken.class, token);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
}
