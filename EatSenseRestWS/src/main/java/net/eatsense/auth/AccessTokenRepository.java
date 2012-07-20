package net.eatsense.auth;

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
	
	public AccessToken create(TokenType type, Key<Account> accountKey, Date expires) {
		AccessToken token = new AccessToken(IdHelper.generateId(), type, accountKey);
		ofy().put(token);
		return token;
	}
	
	public AccessToken get(String token) {
		try {
			return ofy().get(AccessToken.class, token);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
}
