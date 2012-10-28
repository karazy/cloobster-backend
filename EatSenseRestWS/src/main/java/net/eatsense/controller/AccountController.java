package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Role;
import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Company;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.event.ResetAccountPasswordEvent;
import net.eatsense.event.UpdateAccountPasswordEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.CustomerProfileRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessAccountDTO;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.CustomerAccountDTO;
import net.eatsense.representation.CustomerProfileDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.RecipientDTO;
import net.eatsense.representation.RegistrationDTO;
import net.eatsense.service.FacebookService;
import net.eatsense.validation.CockpitUserChecks;
import net.eatsense.validation.EmailChecks;
import net.eatsense.validation.LoginNameChecks;
import net.eatsense.validation.PasswordChecks;
import net.eatsense.validation.ValidationHelper;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.tools.development.DynamicLatencyAdjuster.Default;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

/**
 * Manages Account creation, updates and authentication.
 * 
 * @author Nils Weiher
 *
 */
public class AccountController {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AccountRepository accountRepo;
	private final BusinessRepository businessRepo;
	private final NewsletterRecipientRepository recipientRepo;
	private final ValidationHelper validator;
	private final CompanyRepository companyRepo;
	private final FacebookService facebookService;
	private final ImageController imageController;
	private final AccessTokenRepository accessTokenRepo;
	private final EventBus eventBus;
	private final CustomerProfileRepository customerProfileRepo;
	private final CheckInRepository checkInRepo;

	@Inject
	public AccountController(AccountRepository accountRepo, BusinessRepository businessRepository,
			NewsletterRecipientRepository recipientRepo, CompanyRepository companyRepo,
			ValidationHelper validator, FacebookService facebookService,
			ImageController imageController, AccessTokenRepository accessTokenRepo, EventBus eventBus, CustomerProfileRepository customerProfileRepo, CheckInRepository checkInRepo) {
		super();
		this.accessTokenRepo = accessTokenRepo;
		this.validator = validator;
		this.recipientRepo = recipientRepo;
		this.businessRepo = businessRepository;
		this.accountRepo = accountRepo;
		this.companyRepo = companyRepo;
		this.facebookService = facebookService;
		this.imageController = imageController;
		this.checkInRepo = checkInRepo;
		this.eventBus = eventBus;
		this.customerProfileRepo = customerProfileRepo;
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
		logger.warn("Account({}) has no right to access Business({})",account.getId(), businessId);
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
		// Inactive accounts have no role.
		if(!account.isActive())
			return false;
		
		// grant the user role, if an account was authenticated and is active.
    	if(role.equals(Role.USER) && account.getId() != null) {
    		return true;
    	}
		
		if(role.equals(account.getRole()))
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
		
		if(account == null) {
			account = accountRepo.getByProperty("email", login);
		}
		
		if(account == null) {
			return null;
		}
		
		if(!account.isActive())
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
		
		if(account == null) {
			account = accountRepo.getByProperty("email", login);
		}
		
		if(account == null)
			return null;
		
		if(!account.isActive())
			return null;
		
		if( accountRepo.checkPassword(password, account.getHashedPassword())) {
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
	 * Get a list of businesses the given account manages.
	 * 
	 * @param login the login of the account
	 * @return list of BusinessDTO objects the account manages
	 */
	public List<BusinessDTO> getBusinessDtos(String login) {
		Account account = accountRepo.getByProperty("login", login);
		ArrayList<BusinessDTO> businessDtos = new ArrayList<BusinessDTO>();
		if(account != null && account.getBusinesses() != null) {
			for (Business business :businessRepo.getByKeys(account.getBusinesses())) {
				BusinessDTO businessData = new BusinessDTO(business);
				businessDtos.add(businessData);
			}
		}
		
		return businessDtos;
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
		
		validator.validate(recipient);
		
		NewsletterRecipient duplicateRecipient = recipientRepo.getByProperty("email", recipient.getEmail());
		if(duplicateRecipient != null) {
			throw new ValidationException("email already registered");
		}
		
		recipientRepo.saveOrUpdate(recipient);
		
		return recipient;
	}
	
	/**
	 * Create and save an inactive account if all data was validated. 
	 * 
	 * @param accountData
	 * @return
	 */
	public Account registerNewCompanyAccount(RegistrationDTO accountData) {
		checkNotNull(accountData, "accountData was null");
		
		validator.validate(accountData);
		
		checkLoginDoesNotExist(accountData.getLogin());
		checkEmailDoesNotExist(accountData.getEmail());		
		
		Company company = companyRepo.newEntity();
		
		company.setAddress(accountData.getCompany().getAddress());
		company.setCity(accountData.getCompany().getCity());
		company.setCountry(accountData.getCompany().getCountry());
		company.setName(accountData.getCompany().getName());
		company.setPhone(accountData.getCompany().getPhone());
		company.setPostcode(accountData.getCompany().getPostcode());
		
		Key<Company> companyKey = companyRepo.saveOrUpdate(company);
		
		Account account = accountRepo.newEntity();
		
		account.setActive(false);
		account.setCreationDate(new Date());
		account.setName(accountData.getName());
		account.setLogin(accountData.getLogin());
		account.setEmail(accountData.getEmail());
		account.setRole(Role.COMPANYOWNER);
		account.setCompany(companyKey);
		account.setPhone(accountData.getPhone());
			
		account.setEmailConfirmed(false);
		account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
		
		accountRepo.saveOrUpdate(account);
		
		return account;
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
	
	/**
	 * Retrieve the account associated with the given facebook UID and access token.
	 * Only if both belong to the same account, we return the account object.
	 * 
	 * @param uid
	 * @param accessToken
	 * @return {@link Account} saved with the given uid
	 */
	public Account authenticateFacebook(String uid, String accessToken) {
		checkArgument( !Strings.nullToEmpty(uid).isEmpty(), "uid was null or empty");
		checkArgument( !Strings.nullToEmpty(accessToken).isEmpty(), "accessToken was null or empty");
		
		Account account = null;
		// Return the user object from the facebook api, to which the access token belongs.
		JSONObject jsonMe;
		try {
			jsonMe = facebookService.getMe(accessToken);
		} catch (IllegalArgumentException e) {
			throw new IllegalAccessException("invalid accessToken");
		} 
		String facebookUid = jsonMe.optString("id");
		if( uid.equals( facebookUid)) {
			logger.info("Valid access token recieved for {}", jsonMe.optString("name"));
			account = accountRepo.getByProperty( "facebookUid", facebookUid);
			if(account == null) {
				throw new IllegalAccessException("unregistered facebook user " + jsonMe.optString("name"));
			}
			else
				return account;
		}
		else {
			throw new IllegalAccessException("facebook uid not belonging to access token, token owner: " + jsonMe.optString("name"));
		}
	}
	
	/**
	 * Set an account email confirmed.
	 * 
	 * @param confirmationData
	 * @return
	 */
	public Account confirmAccountEmail(Key<Account> accountKey) {
		checkNotNull(accountKey, "accountKey was null");
		
		Account account;
		try {
			account = accountRepo.getByKey(accountKey);
		} catch (NotFoundException e1) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		if(!account.isEmailConfirmed() ) {
			if(!Strings.isNullOrEmpty(account.getNewEmail())) {
				account.setEmail(account.getNewEmail());
				account.setNewEmail(null);
			}
			account.setEmailConfirmed(true);
			// Activate account.
			account.setActive(true);
			accountRepo.saveOrUpdate(account);
		}
		
		return account;
	}
	
	public ImageDTO updateCompanyImage(Account account, Company company, ImageDTO updatedImage) {
		checkNotNull(company, "company was null");
		checkNotNull(updatedImage, "updatedImage was null ");
		checkArgument(!Strings.isNullOrEmpty(updatedImage.getId()), "updatedImage id was null or empty");
		
		UpdateImagesResult result = imageController.updateImages(account, company.getImages(), updatedImage);
		
		if(result.isDirty()) {
			company.setImages(result.getImages());
			companyRepo.saveOrUpdate(company);
		}
		
		return result.getUpdatedImage();
	}
	
	/**
	 * @param company
	 * @return new instance of a data transfer object
	 */
	public CompanyDTO toCompanyDTO(Company company) {
		if(company == null)
			return null;
		
		return new CompanyDTO(company);
	}


	/**
	 * Update a Company entity with new data.
	 * 
	 * @param company - The entity to update.
	 * @param companyData - The data to update the entity with.
	 * @return updated transfer object for the Company entity
	 */
	public CompanyDTO updateCompany(Company company, CompanyDTO companyData) {
		checkNotNull(company, "company was null");
		checkNotNull(companyData, "companyData was null");
		
		validator.validate(companyData);
		
		company.setAddress(companyData.getAddress());
		company.setCity(companyData.getCity());
		company.setCountry(companyData.getCountry());
		company.setName(companyData.getName());
		company.setPhone(companyData.getPhone());
		company.setPostcode(companyData.getPostcode());
		company.setUrl(companyData.getUrl());
		
		if(company.isDirty()) {
			companyRepo.saveOrUpdate(company);
			return new CompanyDTO(company);
		}
		else {
			return companyData;
		}
	}
	
	/**
	 * Ensures a given login is not already in-use.
	 * 
	 * @param login
	 * @return <code>true</code>
	 * @throws ValidationException If the "login" is already in-use.
	 */
	public boolean checkLoginDoesNotExist(String login) throws ValidationException {
		checkNotNull(login, "login was null");
		
		if(accountRepo.getKeyByProperty("login", login) != null)
			throw new ValidationException("Login already in use.", "error.account.login.exists");
		else
			return true;
	}
	
	/**
	 * Ensures a given email is not already in-use.
	 * 
	 * @param email
	 * @return <code>true</code>
	 * @throws ValidationException If the "email" is already in-use.
	 */
	public boolean checkEmailDoesNotExist(String email) throws ValidationException {
		checkNotNull(email, "login was null");
		
		if(accountRepo.getKeyByProperty("email", email) != null)
			throw new ValidationException("E-mail adress already in use.", "error.account.email.exists");
		else
			return true;
	}
	
	/**
	 * Create a new cockpit user for a company and grant permission for specified businesses.
	 * 
	 * @param ownerAccount Account entity initiating the creation.
	 * @param accountData Containing login, password, business id (for which the account should have access), and an optional name.
	 * @return a new Account entity
	 */
	public BusinessAccountDTO createCockpitUserAccount(Account ownerAccount, BusinessAccountDTO accountData) {
		checkNotNull(ownerAccount, "ownerAccount was null");
		checkNotNull(accountData, "accountData was null");
		
		Account account = accountRepo.newEntity();
		account.setCreationDate(new Date());
		account.setCompany(ownerAccount.getCompany());
		account.setRole(Role.COCKPITUSER);
		account.setActive(true);
		
		return updateCompanyAccount(account, ownerAccount, accountData);
	}
	
	/**
	 * Create a new account with a supplied e-mail address or add an existing
	 * account with that e-mail address to the company and grant the
	 * {@link Role#BUSINESSADMIN}.
	 * 
	 * @param ownerAccount {@link Account} initiating the creation/addition.
	 * @param accountData Only "email" and "name" will be used.
	 * @return The newly created or updated {@link Account}.
	 */
	public Account createOrAddAdminAccount(Account ownerAccount, BusinessAccountDTO accountData) {
		checkNotNull(ownerAccount, "ownerAccount was null");
		checkNotNull(accountData, "accountData was null");
		
		validator.validate(accountData, EmailChecks.class);
		
		Account account = accountRepo.getByProperty("email", accountData.getEmail());
		
		if(account== null) {
			account = accountRepo.newEntity();
			account.setCreationDate(new Date());
			account.setActive(false);
			account.setEmail(accountData.getEmail());
			account.setName(accountData.getName());
		}
		else {
			if(account.getCompany() != null) {
				throw new ValidationException("Account with that e-mail belongs to a different company.");
			}
			if(!account.isActive()) {
				throw new ValidationException("Existing account must be active to add as administrator.");
			}
		}
		
		account.setCompany(ownerAccount.getCompany());
		account.setRole(Role.BUSINESSADMIN);
		
		accountRepo.saveOrUpdate(account);
		
		return account;
	}
	
	/**
	 * Update specific fields for a business user Account entity.
	 * 
	 * @param account
	 * @param accountData
	 * @return
	 */
	public Account updateBusinessAccount(Account account, BusinessAccountDTO accountData) {
		checkNotNull(account, "account was null");
		checkNotNull(accountData, "accountData was null");
		
		account.setPhone(accountData.getPhone());
		
		return updateAccount(account, accountData);
	}
	
	public Account updateCustomerAccount(Account account , CustomerAccountDTO accountData) {
		checkNotNull(account, "account was null");
		checkNotNull(accountData, "accountData was null");
		
		// Change 
		if(!Strings.isNullOrEmpty(accountData.getCheckInId()) && account.getActiveCheckIn() == null) {
			CheckIn checkIn = checkInRepo.getByProperty("userId", accountData.getCheckInId());
			
			if(checkIn == null) {
				throw new ValidationException("CheckIn unknown", "account.error.checkin.unknown");
			}
			else {
				account.setActiveCheckIn(checkIn.getKey());
				checkIn.setAccount(account.getKey());
				checkInRepo.saveOrUpdate(checkIn);
			}
		}		
		
		return updateAccount(account, accountData, true);
	}
	
	/**
	 * Update account profile data.
	 * 
	 * @param account
	 * @param accountData
	 * @return
	 */
	public Account updateAccount(Account account, AccountDTO accountData) {
		return updateAccount(account, accountData, false);
	}


	/**
	 * Update account profile data.
	 * 
	 * @param account
	 * @param accountData
	 * @param emailConfirmed If <code>true</code>, send e-mail confirmation before updating the e-mail field.
	 * @return
	 */
	public Account updateAccount(Account account, AccountDTO accountData, boolean emailConfirmed) {
		checkNotNull(account, "account was null");
		checkNotNull(accountData, "accountData was null");
		
		// Validate data for cockpit user update.
		validator.validate(accountData, EmailChecks.class);
		
		account.setName(accountData.getName());
		
		if(!Objects.equal(account.getEmail(),accountData.getEmail()) || 
				!Objects.equal(account.getNewEmail(), accountData.getEmail())) {
			if(account.getNewEmail() != null || !account.isEmailConfirmed()) {
				// User wants to set a new email while already in the process of
				// confirming another address.
				List<Key<AccessToken>> tokenKeys = accessTokenRepo.getKeysForAccountAndType(account.getKey(), TokenType.EMAIL_CONFIRMATION);
				// Delete all previous confirmation tokens.
				accessTokenRepo.delete(tokenKeys);
			}
			if(account.getEmail().equals(accountData.getEmail())) {
				// User changed the e-mail  back to the original one.
				account.setEmailConfirmed(true);
				account.setNewEmail(null);
			}
			else {
				checkEmailDoesNotExist(accountData.getEmail());
				if(!emailConfirmed) {
					account.setNewEmail(accountData.getEmail());
					
				}
				else {
					account.setEmail(accountData.getEmail());
					logger.info("Writing new e-mail address {}", account.getEmail());
				}
				
				account.setEmailConfirmed(false);
			}
		}

		if(!Objects.equal(account.getLogin(), Strings.emptyToNull(accountData.getLogin()))) {
			validator.validate(accountData, LoginNameChecks.class);
			checkLoginDoesNotExist(accountData.getLogin());
			account.setLogin(accountData.getLogin());
		}

		if(!Strings.isNullOrEmpty(accountData.getPassword())) {
			validator.validate(accountData, PasswordChecks.class);
			// If we get a new password supplied, hash and save it.
			account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
			//Invalidate all session tokens.
			List<Key<AccessToken>> authTokens = accessTokenRepo.getKeysForAccountAndType(account.getKey(), TokenType.AUTHENTICATION);
			accessTokenRepo.delete(authTokens);
			// MailController listens for this type of event.
			eventBus.post(new UpdateAccountPasswordEvent(account));
		}

		if(account.isDirty()) {
			accountRepo.saveOrUpdate(account);
		}
		
		return account;
	}
	
		
	/**
	 * Update permissions and/or account data.
	 * 
	 * @param account
	 * @param ownerAccount Account entity initiating the update.
	 * @param accountData Updated fields for login, password,name and/or business ids.
	 * @return Updated transfer object.
	 */
	public BusinessAccountDTO updateCompanyAccount(Account account, Account ownerAccount, BusinessAccountDTO accountData) {
		checkNotNull(account, "account was null");
		checkNotNull(ownerAccount, "ownerAccount was null");
		checkNotNull(accountData, "accountData was null");
		
		if(!account.getCompany().equals(ownerAccount.getCompany())) {
			throw new IllegalAccessException("Can only update company accounts");
		}
		
		ArrayList<Key<Business>> businessKeys = new ArrayList<Key<Business>>();
		
		if(accountData.getBusinessIds() != null) {
			for (Long businessId : accountData.getBusinessIds()) {
				Key<Business> businessKey = businessRepo.getKey(businessId);
				// Check that we only add business keys, that come from the owner account.
				if(ownerAccount.getBusinesses().contains(businessKey) && !businessKeys.contains(businessKey)) {
					businessKeys.add(businessKey);
				}
				else {
					throw new ValidationException("Cannot create account with a business id that is not from the owner account.");
				}
			}
		}
		account.setBusinesses(businessKeys);
		
		if(account.getRole().equals(Role.COCKPITUSER)) {
			// Validate data for cockpit user update.
			validator.validate(accountData, CockpitUserChecks.class);
			
			// Update of Account Data by the company owner is only allowed for
			// cockpit user accounts.
			account.setName(accountData.getName());

			if(!Objects.equal(account.getLogin(),accountData.getLogin())) {
				checkLoginDoesNotExist(accountData.getLogin());
				account.setLogin(accountData.getLogin());
			}

			if(!Strings.isNullOrEmpty(accountData.getPassword())) {
				validator.validate(accountData, PasswordChecks.class);
				// If we get a new password supplied, hash and save it.
				account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
			}
		}
		
		if(account.isDirty()) {
			accountRepo.saveOrUpdate(account);
		}
		
		return new BusinessAccountDTO(account);
	}
	
	/**
	 * Delete the account if it is a cokpituser account. If it is a business
	 * admin account, just remove the association to the company and company
	 * businesses.
	 * 
	 * @param account
	 */
	public void deleteCompanyUserAccount(Account account) {
		checkNotNull(account, "account was null");
		
		// Delete the account if it was a cockpituser.
		if(account.getRole().equals(Role.COCKPITUSER)) {
			accountRepo.delete(account);
			return;
		}
		
		// Don't delete the account, only delete the association with the
		// company and businesses.
		if(account.getRole().equals(Role.BUSINESSADMIN)) {
			account.setCompany(null);
			account.setBusinesses(null);
			accountRepo.saveOrUpdate(account);
			return;
		}
	}
	
	/**
	 * @param companyKey
	 * @param role If specified, only return Accounts with this role.
	 * @return List of Account transfer objects for the company filtered by role if specified. 
	 */
	public List<BusinessAccountDTO> getCompanyAccounts(Account authenticatedAccount, Key<Company> companyKey, String role) {
		checkNotNull(companyKey, "companyKey was null");
		
		ArrayList<BusinessAccountDTO> accountDtos = new ArrayList<BusinessAccountDTO>();
		List<Account> accounts;
		
		if(!Strings.isNullOrEmpty(role)) {
			if(!Role.BUSINESSADMIN.equals(role) && !Role.COCKPITUSER.equals(role)) {
				throw new ValidationException("Invalid role specified.");
			}
			
			if(Role.BUSINESSADMIN.equals(role) && !authenticatedAccount.getRole().equals(Role.COMPANYOWNER)) {
				// Business admins cannot get other business admin accounts.
				accounts = Collections.emptyList();
			}
			else {
				accounts = accountRepo.getAccountsByCompanyAndRole(companyKey, role);
			}
			
		}
		else {
			accounts = accountRepo.getListByProperty("company", companyKey);
		}
		
		for (Account account : accounts) {
			if(!account.getId().equals(authenticatedAccount.getId())) {
				// Filter the account making the request.
				accountDtos.add(new BusinessAccountDTO(account));
			}
		}
		
		return accountDtos;
	}
	
	/**
	 * @param id
	 * @param companyKey 
	 * @return Account entity saved with the specified id.
	 */
	public Account getAccountForCompany(long id, Key<Company> companyKey) {
		Account account;
		try {
			account = accountRepo.getById(id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		if(companyKey.equals(account.getCompany()))
			return account;
		else
			throw new net.eatsense.exceptions.NotFoundException();
	}
	
	/**
	 * Return accounts with that e-mail, should always return one element in the list or an empty list.
	 * 
	 * @param email
	 * @return
	 */
	public List<BusinessAccountDTO> getAccountsByEmail(String email) {
		ArrayList<BusinessAccountDTO> accountDTOs = new ArrayList<BusinessAccountDTO>();
		
		for(Account account : accountRepo.getListByProperty("email", email)) {
			BusinessAccountDTO accountDTO = new BusinessAccountDTO();
			if(account.getCompany() != null) {
				accountDTO.setCompanyId(account.getCompany().getId());
			}
			accountDTO.setName(account.getName());
			accountDTO.setEmail(account.getEmail());
			
			accountDTOs.add(accountDTO);
		}
		return accountDTOs;
	}


	/**
	 * @param accessToken
	 * @param accountData
	 * @return
	 */
	public BusinessAccountDTO setupAdminAccount(Key<Account> accountKey, BusinessAccountDTO accountData) {
		checkNotNull(accountKey, "accountKey was null");
		checkNotNull(accountData, "accountData was null");
		
		Account account = accountRepo.getByKey(accountKey);
		
		validator.validate(accountData, PasswordChecks.class);
		
		// If we get a new password supplied, hash and save it.
		account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
		account.setActive(true);
		account.setEmailConfirmed(true);
		
		accountRepo.saveOrUpdate(account);
		
		return new BusinessAccountDTO(account);
	}
	
	/**
	 * Return a new unique access token for account setup.
	 * 
	 * @param account
	 * @return {@link AccessToken} with {@link TokenType#ACCOUNTSETUP}
	 */
	public AccessToken createSetupAccountToken(Account account) {
		Calendar cal = Calendar.getInstance();
		//TODO: Extract expiration date for account setup token.
    	cal.add(Calendar.DAY_OF_MONTH, 7);
		
		return accessTokenRepo.create(TokenType.ACCOUNTSETUP, account.getKey(), cal.getTime());
	}
	
	/**
	 * Create and save a new access token for authentication, valid for several days.
	 * 
	 * @param account
	 * @return {@link AccessToken} with {@link TokenType#AUTHENTICATION}
	 */
	public AccessToken createAuthenticationToken(Account account) {
		
		return accessTokenRepo.createAuthToken(account.getKey(), false);
	}
	
	/**
	 * Create and save a new access token for authentication, valid until invalidated.
	 * 
	 * @param account
	 * @return {@link AccessToken} with {@link TokenType#AUTHENTICATION}
	 */
	public AccessToken createAuthenticationToken(Account account, boolean isPermanent) {
		return accessTokenRepo.createAuthToken(account.getKey(), isPermanent);
	}
	
	/**
	 * Create and save new access token for authentication from the app.
	 * 
	 * @param account
	 * @return New {@link AccessToken} with {@link TokenType#AUTHENTICATION_CUSTOMER} and no expiration date.
	 */
	public AccessToken createCustomerAuthToken(Account account) {
		
		Calendar cal = Calendar.getInstance();
		//TODO: Extract expiration date for app tokens.
    	cal.add(Calendar.YEAR, 1);
		
		return accessTokenRepo.create(TokenType.AUTHENTICATION_CUSTOMER, account.getKey(), cal.getTime());
	}

	/**
	 * @param account
	 * @return
	 */
	public AccessToken createEmailConfirmationToken(Account account) {
		Calendar cal = Calendar.getInstance();
		//TODO: Extract expiration date for confirm account token.
    	cal.add(Calendar.YEAR, 1);

		return accessTokenRepo.create(TokenType.EMAIL_CONFIRMATION, account.getKey(), cal.getTime());
	}
	
	/**
	 * @param account
	 * @return
	 */
	public AccessToken createPasswordResetToken(Account account) {
		Calendar cal = Calendar.getInstance();
		//TODO: Extract expiration date for confirm account token.
    	cal.add(Calendar.DATE, 1);

		return accessTokenRepo.create(TokenType.PASSWORD_RESET, account.getKey(), cal.getTime());
	}

	/**
	 * @param email
	 * @param uriInfo
	 */
	public void createAndSendPasswordResetToken(String email, UriInfo uriInfo) {
		if(Strings.isNullOrEmpty(email)) {
			throw new ValidationException("No e-mail address provided.");
		}
		
		Account account = accountRepo.getByProperty("email", email);
		//TODO Remove return values.
		if(account == null) {
			throw new ValidationException("Unknown e-mail address.");
		}
		// Delete old tokens.
		List<Key<AccessToken>> tokens = accessTokenRepo.getKeysForAccountAndType(account.getKey(), TokenType.PASSWORD_RESET);
		accessTokenRepo.delete(tokens);
		
		eventBus.post(new ResetAccountPasswordEvent(account, uriInfo));
	}

	/**
	 * 
	 * @param token
	 * @param accountData
	 */
	public void resetPassword(String token, BusinessAccountDTO accountData) {
		checkArgument(!Strings.isNullOrEmpty(token), "token was null or empty");
		checkNotNull(accountData, "accountData was null");
		
		AccessToken accessToken = accessTokenRepo.get(token);
		if(accessToken.getExpires()!= null && accessToken.getExpires().before(new Date())) {
			throw new IllegalAccessException("Token expired, request a new token.");
		}
		
		Account account = accountRepo.getByKey(accessToken.getAccount());
		
		if(account == null) {
			accessTokenRepo.delete(accessToken);
			throw new ValidationException("Token invalid or account no longer exists.");
		}
		
		validator.validate(accountData, PasswordChecks.class);
		
		// If we get a new password supplied, hash and save it.
		account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
		accountRepo.saveOrUpdate(account);
		
		List<Key<AccessToken>> authTokens = accessTokenRepo.getKeysForAccountAndType(account.getKey(), TokenType.AUTHENTICATION);
		authTokens.add(accessToken.getKey());
		accessTokenRepo.delete(authTokens);
	}
	
	public Account removeActiveCheckIn(Account account) {
		checkNotNull(account, "account was null");
		
		account.setActiveCheckIn(null);
		accountRepo.saveOrUpdate(account);
		return account;
	}
	
	
	/**
	 * create and save a new Account entity with the supplied data and the "user" role.
	 * 
	 * @param accountData
	 * @return
	 */
	public Account registerNewCustomerAccount(CustomerAccountDTO accountData) {
		checkNotNull(accountData, "accountData was null");
		
		validator.validate(accountData, javax.validation.groups.Default.class, PasswordChecks.class);
		checkEmailDoesNotExist(accountData.getEmail());
		
		if(!Strings.isNullOrEmpty(accountData.getLogin())) {
			validator.validate(accountData, LoginNameChecks.class);
			checkLoginDoesNotExist(accountData.getLogin());
		}
		
		Account account = accountRepo.newEntity();
		CustomerProfile profile = customerProfileRepo.newEntity();
		
		account.setActive(true);
		account.setCreationDate(new Date());
		account.setEmail(accountData.getEmail());
		account.setNewEmail(accountData.getNewEmail());
		account.setEmailConfirmed(false);
		account.setHashedPassword(accountRepo.hashPassword(accountData.getPassword()));
		account.setCustomerProfile(customerProfileRepo.saveOrUpdate(profile));
		account.setName(accountData.getName());
		account.setLogin(accountData.getLogin());
		account.setRole(Role.USER);
		
		boolean isCheckInSet = !Strings.isNullOrEmpty(accountData.getCheckInId());
		CheckIn checkIn = null;
		if(isCheckInSet) {
			checkIn = checkInRepo.getByProperty("userId", accountData.getCheckInId());
			if(checkIn == null) {
				throw new ValidationException("CheckIn unknown", "account.error.checkin.unknown");
			}
			else {
				account.setActiveCheckIn(checkIn.getKey());
			}
		}
		
		Key<Account> accountKey = accountRepo.saveOrUpdate(account);
		
		if(checkIn != null) {
			checkIn.setAccount(accountKey);
			checkInRepo.saveOrUpdate(checkIn);
		}
				
		return account;
	}
	
	/**
	 * @param account to link the profile with.
	 * @return profile entity
	 */
	public CustomerProfile addCustomerProfile(Account account) {
		checkNotNull(account, "account was null");
		
		if(account.getCustomerProfile() != null) {
			logger.warn("Returning already existing profile for Account({})", account.getId());
			try {
				return customerProfileRepo.getByKey(account.getCustomerProfile());
			} catch (NotFoundException e) {
				logger.warn("Could not load profile for Account({}). Creating new profile.", account.getId());
			}
		}
		
		CustomerProfile profile = customerProfileRepo.newEntity();
		
		account.setCustomerProfile(customerProfileRepo.saveOrUpdate(profile));
		accountRepo.saveOrUpdate(account);
		
		return profile;
	}
}
