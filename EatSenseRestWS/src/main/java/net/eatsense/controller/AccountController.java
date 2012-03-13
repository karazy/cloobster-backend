package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;

/**
 * Manages Account creation, updates and authentication.
 * 
 * @author Nils Weiher
 *
 */
public class AccountController {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private AccountRepository accountRepo;
	private RestaurantRepository restaurantRepo;
	
	@Inject
	public AccountController(AccountRepository accountRepo, RestaurantRepository rr) {
		super();
		this.restaurantRepo = rr;
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
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();	
		
		if( account.getHashedPassword().equals(hashedPassword) ) {
			// Reset failed attempts counter
			if(account.getFailedLoginAttempts() > 0) {
				account.setFailedLoginAttempts(0);
				accountRepo.saveOrUpdate(account);
			}
			
//			if(account.getChannelToken() == null || account.getChannelToken().isEmpty()) {
				
				String token = channelService.createChannel(account.getLogin());
				logger.debug("created channel token "+token);
				account.setChannelToken(token);
//			}
			
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
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();		
		
		if( BCrypt.checkpw(password, account.getHashedPassword() )) {
			// Reset failed attempts counter
			if(account.getFailedLoginAttempts() > 0) {
				account.setFailedLoginAttempts(0);
				accountRepo.saveOrUpdate(account);
			}
			
//			if(account.getChannelToken() == null || account.getChannelToken().isEmpty()) {
				String token = channelService.createChannel(account.getLogin());
				logger.debug("created channel token "+token);
				account.setChannelToken(token);
//			}
			
			
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
		accountData.setToken(account.getChannelToken());
		return accountData;
	}
	
	public AccountDTO getAccount(String login, String password) {
		return toDto(authenticate(login, password));
	}


	public List<BusinessDTO> getBusinessDtos(String login) {
		Account account = accountRepo.getByProperty("login", login);
		ArrayList<BusinessDTO> businessDtos = new ArrayList<BusinessDTO>();
		if(account != null && account.getRole().equals("restaurantadmin")) {
			for (Restaurant restaurant :restaurantRepo.getAll()) {
				BusinessDTO businessData = new BusinessDTO();
				businessData.setId(restaurant.getId());
				businessData.setName(restaurant.getName());
				businessData.setDescription(restaurant.getDescription());
				businessDtos.add(businessData);
			}
		}
			
		return businessDtos;
	}
}
