package net.eatsense.auth;

import java.util.Date;

import javax.persistence.Id;

import net.eatsense.domain.Account;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

@Cached
public class AccessToken {
	public enum TokenType {
		AUTHENTICATION,
		AUTHENTICATION_CUSTOMER,
		EMAIL_CONFIRMATION,
		PASSWORD_RESET,
		/**
		 * Token used for setup of a new administrator account.
		 */
		ACCOUNTSETUP
	}

	@Id
	private String token;
	private TokenType type;
	private Date expires;
	private Key<Account> account;
	
	public AccessToken() {
		super();
	}

	public AccessToken(String token, TokenType type, Date expires,
			Key<Account> account) {
		super();
		this.token = token;
		this.type = type;
		this.expires = expires;
		this.account = account;
	}

	public AccessToken(String token, Key<Account> account) {
		super();
		this.type = TokenType.AUTHENTICATION;
		this.account = account;
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public Key<Account> getAccount() {
		return account;
	}

	public void setAccount(Key<Account> account) {
		this.account = account;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}
	
	public Key<AccessToken> getKey() {
		return new Key<AccessToken>(AccessToken.class, token);
	}
}
