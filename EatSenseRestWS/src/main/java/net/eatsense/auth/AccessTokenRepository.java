package net.eatsense.auth;

import java.util.Date;

import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.domain.Account;
import net.eatsense.util.IdHelper;

import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

@Singleton
public class AccessTokenRepository extends DAOBase {
	static {
		ObjectifyService.register(AccessToken.class);
	}
	
	public AccessToken createAccessToken(TokenType type, Key<Account> accountKey, Date expires) {
		AccessToken token = new AccessToken(IdHelper.generateId(), type, accountKey);
		ofy().put(token);
		return token;
	}
}
