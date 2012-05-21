package net.eatsense.persistence;

import java.util.Date;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;

import org.mindrot.jbcrypt.BCrypt;

import com.googlecode.objectify.Key;

public class AccountRepository extends GenericRepository<Account> {

	final static Class<Account> entityClass = Account.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	
	public AccountRepository() {
		super(entityClass);
	}
	
	/**
	 * Create and save the account in the datastore. password is encrypted as bcrypt hash.
	 * 
	 * @param login
	 * @param password
	 * @param email
	 * @param role
	 * @param businessKeys
	 * @param emailConfirmed 
	 * @param active 
	 * @return
	 */
	public Account createAndSaveAccount(String login, String password, String email, String role, List<Key<Business>> businessKeys, boolean emailConfirmed, boolean active) {
		Account account = new Account();
		account.setLogin(login);
		account.setEmail(email);
		account.setRole(role);
		account.setBusinesses(businessKeys);
		account.setEmailConfirmed(emailConfirmed);
		account.setActive(active);
		account.setCreationDate(new Date());		
		account.setHashedPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		
		if(saveOrUpdate(account) == null)
			return null;
		
		return account;
	}
}
