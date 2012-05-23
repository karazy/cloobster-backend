package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.auth.Role;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.exceptions.RegistrationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.RecipientDTO;
import net.eatsense.representation.RegistrationDTO;
import net.eatsense.util.IdHelper;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
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
	private CompanyRepository companyRepo;
		
	@Inject
	public AccountController(AccountRepository accountRepo, BusinessRepository businessRepository,
			NewsletterRecipientRepository recipientRepo, CompanyRepository companyRepo,
			ChannelController cctrl, Validator validator) {
		super();
		this.validator = validator;
		this.recipientRepo = recipientRepo;
		this.channelCtrl = cctrl;
		this.businessRepo = businessRepository;
		this.accountRepo = accountRepo;
		this.companyRepo = companyRepo;
	}
	
	
	/**
	 * Check if the account is allowed to manage a given business.
	 * 
	 * @param account
	 * @param businessId identifying the business to check for
	 * @return
	 */
	public boolean isAccountManagingBusiness(final Account account, long businessId) {
		if(account == null)
			return false;
		
		if(account.getBusinesses() != null) {
			for (Key<Business> businessKey : account.getBusinesses()) {
				if(businessKey.getId() == businessId) 
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the account is in a specific role.
	 * 
	 * @param account
	 * @param role
	 * @return 	<code>true</code> if the account is in the role<br>
	 * 			<code>false</code> otherwise
	 */
	public boolean isAccountInRole(final Account account, String role) {
		if(role == null || role == "")
			return true;
		
		if(account == null)
			return false;
		
		// grant the user role, if an account was authenticated
    	if(role.equals(Role.USER) && account.getId() != null) {
    		return true;
    	}
		
		if(role.equals(account.getRole()))
			return true;
	
    	// grant the cockpituser role too if the account is in businessadmin or companyowner role.
		if(role.equals(Role.COCKPITUSER)
				&& ( account.getRole().equals(Role.BUSINESSADMIN) || account.getRole().equals(Role.COMPANYOWNER) ))
			return true;
		
    	// grant the businessadmin role too if the account is in companyowner role.		
		if(role.equals(Role.BUSINESSADMIN) && ( account.getRole().equals(Role.COMPANYOWNER)))
			return true;
					
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
		login = Strings.nullToEmpty(login).toLowerCase();
		
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
		login = Strings.nullToEmpty(login).toLowerCase();
		
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
		if(account != null && account.getBusinesses() != null && isAccountInRole(account, "cockpituser")) {
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
	
	public RegistrationDTO registerNewAccount(RegistrationDTO accountData) {
		checkNotNull(accountData.getLogin(), "accountData login was null");
		checkNotNull(accountData.getEmail(), "accountData email was null");
		checkNotNull(accountData.getPassword(), "accountData password was null");
		checkNotNull(accountData.getCompany(), "accountData company was null");
		checkArgument(!accountData.getLogin().isEmpty(), "accountData login was empty");
		checkArgument(!accountData.getEmail().isEmpty(), "accountData email was empty");
		checkArgument(!accountData.getPassword().isEmpty(), "accountData password was empty");
		checkCompany(accountData.getCompany());
		
		if( accountRepo.getKeyByProperty("email", accountData.getEmail()) != null ) {
			throw new RegistrationException("email already in use", "registrationErrorEmailExists");
		}
		if( accountRepo.getKeyByProperty("login", accountData.getLogin()) != null ) {
			throw new RegistrationException("login already in use", "registrationErrorLoginExists");
		}
		
		Account account = new Account();
		Company company = new Company();
		
		company.setAddress(accountData.getCompany().getAddress());
		company.setCity(accountData.getCompany().getCity());
		company.setCountry(accountData.getCompany().getCountry());
		company.setName(accountData.getCompany().getName());
		company.setPhone(accountData.getCompany().getPhone());
		company.setPostcode(accountData.getCompany().getPostcode());
		Key<Company> companyKey = companyRepo.saveOrUpdate(company);
		
		account.setActive(false);
		account.setCompany(companyKey);
		account.setCreationDate(new Date());
		account.setEmail(accountData.getEmail());
		account.setEmailConfirmationHash( IdHelper.generateId() );
		account.setEmailConfirmed(false);
		account.setLogin(accountData.getLogin());
		account.setName(accountData.getName());
		account.setPhone(accountData.getPhone());
		account.setRole(Role.COMPANYOWNER);
		account.setHashedPassword(BCrypt.hashpw(accountData.getPassword(), BCrypt.gensalt()));
		
		accountRepo.saveOrUpdate(account);
		
		return accountData;
	}
	
	private boolean checkCompany(CompanyDTO company) {
		checkNotNull(company, "company was null");
		checkNotNull(company.getName(), "company name was null");
		checkNotNull(company.getPhone(), "company phone was null");
		checkNotNull(company.getAddress(), "company address was null");
		checkNotNull(company.getCity(), "company address was null");
		checkNotNull(company.getCountry(), "company country was null");
		checkNotNull(company.getPostcode(), "company postcode was null");
		return true;
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
