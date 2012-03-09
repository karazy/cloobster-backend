package net.eatsense.controller;

import java.util.Date;

import net.eatsense.domain.Account;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.AccountDTO;

import org.mindrot.jbcrypt.BCrypt;

import com.google.inject.Inject;

/**
 * Manages Account creation, updates and authentication.
 * 
 * @author Nils Weiher
 *
 */
public class AccountController {
	private AccountRepository accountRepo;
	
	@Inject
	public AccountController(AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
	}
	
	
	/**
	 * Retrieve an account from the store ONLY if the given credentials match.
	 * 
	 * @param login
	 * @param hashedPassword as bcrypt hash
	 * @return the authenticated Account object for the given login
	 */
	public Account authenticateHashed(String login, String hashedPassword) {	
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			return null;
		
		if( account.getHashedPassword().equals(hashedPassword) ) {
			// Reset failed attempts counter
			account.setFailedLoginAttempts(0);
			accountRepo.saveOrUpdate(account);
			return account;
		}			
		else {
			// Increment failed login attempts and set last failed login time
			account.setFailedLoginAttempts(account.getFailedLoginAttempts()+1);
			account.setLastFailedLogin(new Date());
			accountRepo.saveOrUpdate(account);
			return null;
		}
			
	}
	
	/**
	 * Retrieve an account from the store, ONLY if the given credentials match.
	 * 
	 * @param login
	 * @param password cleartext
	 * @return
	 */
	public Account authenticate(String login, String password) {	
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			return null;
		
		if( BCrypt.checkpw(password, account.getHashedPassword() )) {
			// Reset failed attempts counter
			account.setFailedLoginAttempts(0);
			accountRepo.saveOrUpdate(account);
			return account;
		}			
		else {
			// Increment failed login attempts and set last failed login time
			account.setFailedLoginAttempts(account.getFailedLoginAttempts()+1);
			account.setLastFailedLogin(new Date());
			accountRepo.saveOrUpdate(account);
			return null;
		}
			
	}
	
	public AccountDTO updateAccount(AccountDTO accountData) {
		//TODO Validate and save new data for the given account
		return null;
	}
	
	/**
	 * Create and save a new Account, with the given credentials, in the datastore.
	 * 
	 * @param login
	 * @param password
	 * @param email
	 * @param role
	 * @return
	 */
	public Account createAndSaveAccount(String login, String password, String email, String role) {
		return accountRepo.createAndSaveAccount(login, password, email, role);
	}
	public AccountDTO toDto(Account account) {
		if(account == null) {
			return null;
		}
		AccountDTO accountData = new AccountDTO();
		accountData.setLogin(account.getLogin());
		accountData.setRole(account.getRole());
		accountData.setEmail(account.getEmail());
		accountData.setPasswordHash(account.getHashedPassword());
		return accountData;
	}
	
	public AccountDTO getAccount(String login, String password) {
		return toDto(authenticate(login, password));
	}
}
