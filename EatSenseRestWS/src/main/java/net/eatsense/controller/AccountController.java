package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.auth.Role;
import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.RegistrationException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.EmailConfirmationDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.RecipientDTO;
import net.eatsense.representation.RegistrationDTO;
import net.eatsense.service.FacebookService;

import org.codehaus.jettison.json.JSONObject;
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
	private FacebookService facebookService;
	private ImageController imageController;
	private MailController mailCtrl;
	
	@Inject
	public AccountController(AccountRepository accountRepo, BusinessRepository businessRepository,
			NewsletterRecipientRepository recipientRepo, CompanyRepository companyRepo,
			ChannelController cctrl, Validator validator, FacebookService facebookService,
			ImageController imageController, MailController mailCtrl) {
		super();
		this.mailCtrl = mailCtrl;
		this.validator = validator;
		this.recipientRepo = recipientRepo;
		this.channelCtrl = cctrl;
		this.businessRepo = businessRepository;
		this.accountRepo = accountRepo;
		this.companyRepo = companyRepo;
		this.facebookService = facebookService;
		this.imageController = imageController;
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
		if(account == null)
			return null;
		
		if(!account.isActive())
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
		
		return new AccountDTO(account);
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
	 * Create and save an inactive account if all data was validated. 
	 * 
	 * @param accountData
	 * @return
	 */
	public Account registerNewAccount(RegistrationDTO accountData) {
		checkNotNull(accountData, "accountData was null");
		
		Set<ConstraintViolation<RegistrationDTO>> violationSet = validator.validate(accountData);
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<RegistrationDTO> violation : violationSet) {
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new RegistrationException(stringBuilder.toString());
		}
		
		if( accountRepo.getKeyByProperty("email", accountData.getEmail()) != null ) {
			throw new RegistrationException("email already in use", "registrationErrorEmailExists");
		}
		if( accountRepo.getKeyByProperty("login", accountData.getLogin()) != null ) {
			throw new RegistrationException("login already in use", "registrationErrorLoginExists");
		}
		
		Company company = new Company();
		
		company.setAddress(accountData.getCompany().getAddress());
		company.setCity(accountData.getCompany().getCity());
		company.setCountry(accountData.getCompany().getCountry());
		company.setName(accountData.getCompany().getName());
		company.setPhone(accountData.getCompany().getPhone());
		company.setPostcode(accountData.getCompany().getPostcode());
		
		Key<Company> companyKey = companyRepo.saveOrUpdate(company);
		
		Account account = accountRepo.createAndSaveAccount(accountData.getName(), 
				accountData.getLogin(),	accountData.getPassword(), accountData.getEmail(),
				Role.COMPANYOWNER, null, companyKey, accountData.getPhone(),
				accountData.getFacebookUID(), false, false);

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
	 * Confirms an account with the generated token, which was send to the user during registration.
	 * 
	 * @param confirmationData
	 * @return
	 */
	public EmailConfirmationDTO confirmAccountEmail(EmailConfirmationDTO confirmationData) {
		checkNotNull(confirmationData, "confirmationData was null");
		checkArgument(!Strings.nullToEmpty(confirmationData.getConfirmationToken()).isEmpty(), "confirmationToken was null or empty");
		
		Account account = accountRepo.getByProperty("emailConfirmationHash", confirmationData.getConfirmationToken());
		if(account == null)
			throw new ServiceException("Unknown confirmation token");
		
		if(!account.isEmailConfirmed() ) {
			account.setEmailConfirmed(true);
			// Activate account.
			account.setActive(true);
			// Clear the token from the account, so that we get lesser conflicts in the future.
			account.setEmailConfirmationHash(null);
			accountRepo.saveOrUpdate(account);
			
			try {
				mailCtrl.sendAccountConfirmedMessage(account, companyRepo.getByKey(account.getCompany()));
			} catch (MessagingException e) {
				logger.error("error sending confirmation notice", e);
			}
		}
		
		confirmationData.setLogin(account.getLogin());
		confirmationData.setEmailConfirmed(true);
		return confirmationData;
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
		
		Set<ConstraintViolation<CompanyDTO>> violationSet = validator.validate(companyData);
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<CompanyDTO> violation : violationSet) {
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
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
}
