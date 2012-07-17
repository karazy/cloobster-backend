package net.eatsense.auth;

import javax.persistence.Id;

import net.eatsense.domain.Account;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

@Cached
public class AccessToken {
	public enum TokenType {
		AUTHENTICATION,
		EMAIL_CONFIRMATION,
		ACCOUNTSETUP
	}

	@Id
	private String token;
	private TokenType type;
	private Key<Account> account;
	
	public AccessToken() {
		super();
	}

	public AccessToken(String token, TokenType type, Key<Account> account) {
		super();
		this.token = token;
		this.type = type;
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
}
