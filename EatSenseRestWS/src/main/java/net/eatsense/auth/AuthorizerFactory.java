package net.eatsense.auth;

import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

/**
 * Create {@link Authorizer} for handling accounts or checkins.
 * 
 * @author Nils Weiher
 *
 */
public interface AuthorizerFactory {
	/**
	 * @param checkIn
	 * @return Authorizer instance for this checkin.
	 */
	public Authorizer createForCheckIn(final CheckIn checkIn);
	/**
	 * @param account
	 * @param token
	 * @return
	 */
	public Authorizer createForAccount(final Account account, final AccessToken token);
}
