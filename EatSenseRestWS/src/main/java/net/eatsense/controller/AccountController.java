package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Manages Account creation, updates and authentication.
 * 
 * @author Nils Weiher
 *
 */
public class AccountController {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private AccountRepository accountRepo;
	private BusinessRepository businessRepo;
	private ChannelController channelCtrl;
	
	@Inject
	public AccountController(AccountRepository accountRepo, BusinessRepository businessRepository, ChannelController cctrl) {
		super();
		this.channelCtrl = cctrl;
		this.businessRepo = businessRepository;
		this.accountRepo = accountRepo;
	}
	
	
	/**
	 * Check if the account is allowed to manage a given business.
	 * 
	 * @param account
	 * @param businessId identifying the business to check for
	 * @return
	 */
	public boolean isAccountManagingBusiness(final Account account, long businessId){
		
		for (Key<Business> businessKey : account.getBusinesses()) {
			if(businessKey.getId() == businessId) 
				return true;
		}
		return false;
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
			if(account.getFailedLoginAttempts() > 0) {
				account.setFailedLoginAttempts(0);
				accountRepo.saveOrUpdate(account);
			}

			return account;
		}			
		else {
			// Increment failed login attempts and set last failed login time
			account.setFailedLoginAttempts(account.getFailedLoginAttempts()+1);
			account.setLastFailedLogin(new Date());
			accountRepo.saveOrUpdate(account);
			logger.error("Failed login from {}, attempt nr. {}",login,account.getFailedLoginAttempts());
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
			if(account.getFailedLoginAttempts() > 0) {
				account.setFailedLoginAttempts(0);
				accountRepo.saveOrUpdate(account);
			}
		
			return account;
		}			
		else {
			// Increment failed login attempts and set last failed login time
			account.setFailedLoginAttempts(account.getFailedLoginAttempts()+1);
			account.setLastFailedLogin(new Date());
			accountRepo.saveOrUpdate(account);
			logger.error("Failed login from {}, attempt nr. {}",login,account.getFailedLoginAttempts());
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
	 * @param businessIds list of business ids this account manages
	 * @return
	 */
	public Account createAndSaveAccount(String login, String password, String email, String role, List<Long> businessIds) {
		ArrayList<Key<Business>> businessKeys = new ArrayList<Key<Business>>();
		for (Long businessId : businessIds) {
			businessKeys.add( new Key<Business>(Business.class,businessId));
		}
		return accountRepo.createAndSaveAccount(login, password, email, role, businessKeys);
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
	
	/**
	 * Get the account data for the given login and authenticate with the given password.
	 * 
	 * @param login
	 * @param password
	 * @return AccountDTO containing the account data
	 */
	public AccountDTO getAccountDto(String login) {
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			return null;
		
		return toDto(account);
	}

	/**
	 * Get a list of businesses the given account manages.
	 * 
	 * @param login the login of the account
	 * @return list of BusinessDTO objects the account manages
	 */
	public List<BusinessDTO> getBusinessDtos(String login) {
		Account account = accountRepo.getByProperty("login", login);
		ArrayList<BusinessDTO> businessDtos = new ArrayList<BusinessDTO>();
		if(account != null && account.getRole().equals("restaurantadmin")) {
			for (Business business :businessRepo.getByKeys(account.getBusinesses())) {
				BusinessDTO businessData = new BusinessDTO();
				businessData.setId(business.getId());
				businessData.setName(business.getName());
				businessData.setDescription(business.getDescription());
				businessDtos.add(businessData);
			}
		}
			
		return businessDtos;
	}
	
	/**
	 * Generates and returns a new channel token.
	 * 
	 * @param businessId 
	 * @param clientId to use for token creation 
	 * @return the generated channel token
	 */
	public String requestToken (Long businessId, String clientId) {
		logger.debug("new token requested for "+clientId);
		Business business = businessRepo.getById(businessId);
		if(business == null)
			return null;
		return channelCtrl.createCockpitChannel(business, clientId);
	}
}
