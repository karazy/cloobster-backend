package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.event.TransportAdapter;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.RecipientDTO;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Email;
import com.google.apphosting.utils.config.WebXml.SecurityConstraint.TransportGuarantee;
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
	private NewsletterRecipientRepository recipientRepo;
	private Validator validator;
	private MailController mailCtrl;
	
	@Inject
	public AccountController(AccountRepository accountRepo, BusinessRepository businessRepository,
			NewsletterRecipientRepository recipientRepo, ChannelController cctrl, Validator validator) {
		super();
		this.mailCtrl = mailCtrl;
		this.validator = validator;
		this.recipientRepo = recipientRepo;
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
		checkNotNull(account, "Unable to check account, was null");
		
		if(account.getBusinesses() != null) {
			for (Key<Business> businessKey : account.getBusinesses()) {
				if(businessKey.getId() == businessId) 
					return true;
			}
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
		if(businessIds != null) {
			for (Long businessId : businessIds) {
				businessKeys.add( new Key<Business>(Business.class,businessId));
			}
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
		return channelCtrl.createCockpitChannel(businessId, clientId);
	}
	
	/**
	 * Save an email address as a newsletter recipient.
	 * 
	 * @param recipientDto must contain valid email
	 */
	public NewsletterRecipient addNewsletterRecipient(RecipientDTO recipientDto) {
		checkNotNull(recipientDto, "recipientDto was null");
		checkNotNull(recipientDto.getEmail(), "recipientDto email was null");
		
		NewsletterRecipient recipient = new NewsletterRecipient();
		recipient.setEmail(recipientDto.getEmail());
		recipient.setEntryDate(new Date());
		
		Set<ConstraintViolation<NewsletterRecipient>> violations = validator.validate(recipient);
		
		if(!violations.isEmpty()) {
			for (ConstraintViolation<NewsletterRecipient> constraintViolation : violations) {
				throw new IllegalArgumentException(String.format("%s %s",constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
			}
		}
		
		NewsletterRecipient duplicateRecipient = recipientRepo.getByProperty("email", recipient.getEmail());
		if(duplicateRecipient != null) {
			throw new IllegalArgumentException("email already registered");
		}
		
		recipientRepo.saveOrUpdate(recipient);
		
		return recipient;
	}
	
	/**
	 * Remove a newsletter recipient.
	 * 
	 * @param id non-zero
	 * @param email must match email stored under id
	 */
	public void removeNewsletterRecipient(long id, String email) {
		checkArgument(id != 0, "id was 0");
		checkNotNull(email, "email was null");
		checkArgument(!email.isEmpty(), "email was empty");
		
		NewsletterRecipient recipient = recipientRepo.getById(id);
		
		if(recipient.getEmail().equals(email)) {
			recipientRepo.delete(recipient);
		}
		else {
			throw new IllegalArgumentException("email does not match stored address");
		}
	}
}
