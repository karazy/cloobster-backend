package net.eatsense.persistence;

import java.util.Date;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.embedded.UploadToken;
import net.eatsense.util.IdHelper;

import org.mindrot.jbcrypt.BCrypt;

import com.google.common.base.Strings;
import com.googlecode.objectify.Key;

public class AccountRepository extends GenericRepository<Account> {

	final static Class<Account> entityClass = Account.class;
	
	public AccountRepository() {
		super(entityClass);
	}
	
	/**
	 * Create and save the account in the datastore. password is encrypted as bcrypt hash.
	 *
	 * @param name
	 * @param login
	 * @param password
	 * @param email
	 * @param role
	 * @param businessKeys
	 * @param emailConfirmed 
	 * @param active 
	 * @return new Account entity
	 */
	public Account createAndSaveAccount( String name, String login, String password, String email, String role, List<Key<Business>> businessKeys, Key<Company> companyKey, String phone, String facebookUID,  boolean emailConfirmed, boolean active) {
		Account account = new Account();
		account.setActive(active);
		account.setCreationDate(new Date());
		account.setName(name);
		account.setLogin(login);
		account.setEmail(email);
		account.setRole(role);
		account.setBusinesses(businessKeys);
		if(emailConfirmed == false && !Strings.isNullOrEmpty(email)) {
			account.setEmailConfirmationHash(IdHelper.generateId());
		}
		account.setCompany(companyKey);
		account.setPhone(phone);
		account.setFacebookUid(facebookUID);
		account.setEmailConfirmed(emailConfirmed);
		account.setHashedPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		
		if(saveOrUpdate(account) == null)
			return null;
		
		return account;
	}
	
	/**
	 * Hash the password with the bcrypt algorithm and a generated salt.
	 * 
	 * @param password
	 * @return hashed password string
	 */
	public String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	/**
	 * @param companyKey
	 * @param role
	 * @return Accounts belonging to the company with the specified role.
	 */
	public List<Account> getAccountsByCompanyAndRole(Key<Company> companyKey, String role) {
		return ofy().query(Account.class).filter("company", companyKey).filter("role", role).list();
	}
	
	/**
	 * 
	 *  
	 * @return new Token valid 
	 */
	public UploadToken newUploadToken() {
		UploadToken token = new UploadToken();
		token.setToken(IdHelper.generateId());
		token.setCreation(new Date());
		return token;
	}
}
