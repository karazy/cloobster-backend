package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.mail.Message;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.auth.Role;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.RegistrationException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.EmailConfirmationDTO;
import net.eatsense.representation.RecipientDTO;
import net.eatsense.representation.RegistrationDTO;
import net.eatsense.service.FacebookService;

import org.apache.bval.guice.ValidationModule;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private final LocalServiceTestHelper helper =
	    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	private Injector injector;
	private AccountController ctr;
	private BusinessRepository rr;
	private ChannelController channelController;
	
	private String password;
	
	private String login;
	
	private String email;
	
	private String role;
	
	private Account account;
	
	@Mock
	private AccountRepository ar;
	
	@Mock
	private NewsletterRecipientRepository recipientRepo;
	
	@Mock
	private MailController mailCtrl;

	@Mock
	private CompanyRepository companyRepo;

	@Mock
	private FacebookService facebookService;

	@Mock
	private ImageController imageCtrl;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(),
				new ValidationModule());
		channelController = mock(ChannelController.class);
		rr = injector.getInstance(BusinessRepository.class);
		ar = injector.getInstance(AccountRepository.class);
		ValidatorFactory avf = Validation
				.byProvider(ApacheValidationProvider.class).configure()
				.buildValidatorFactory();
		ctr = new AccountController(ar, rr, recipientRepo, companyRepo,
				channelController, avf.getValidator(), facebookService, imageCtrl);

		password = "diesisteintestpasswort";
		login = "testlogin";
		email = "wurst@wurst.de";
		role = "admin";
		// TODO update to use restaurant id
		account = ar.createAndSaveAccount("admin",login, password, email, role,
				rr.getAllKeys(), null, null, null, true, true);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testAuthenticate() {
		//#1 Test correct password
		Account test = ctr.authenticate(login, password);
		
		assertThat(test.getLogin(), is(login));
		assertThat(test.getHashedPassword(), notNullValue());
		assertThat(test.getRole(), is(role));
		assertThat(test.getEmail(), is(email));
	}
	
	@Test
	public void testAuthenticateWrongPassword() {
		assertThat(ctr.authenticate(login, "wrongpassword"), nullValue());
	}
	
	public void testAuthenticateNullPassword() {
		assertThat(ctr.authenticate(login, null), nullValue());
	}
	
	@Test
	public void testAuthenticateNullLogin() {
		assertThat(ctr.authenticate(null, password), nullValue());
	}
	
	@Test
	public void testAuthenticateUnknownLogin() {
		assertThat(ctr.authenticate("notexistinglogin", password), nullValue());
	}
	
	@Test
	public void testAuthenticateCaseInsensitive() {
		String loginWithUpperCase = "TestLogin";		
		Account test = ctr.authenticate(loginWithUpperCase, password);
		
		assertThat(test.getLogin(), is(login));
		assertThat(test.getHashedPassword(), notNullValue());
		assertThat(test.getRole(), is(role));
		assertThat(test.getEmail(), is(email));
	}
		
	
	@Test
	public void testAuthenticateHashed() {
		//#1 Test correct hash
		Account test = ctr.authenticateHashed(login, account.getHashedPassword());
		
		assertThat(test.getLogin(), is(login));
		assertThat(test.getHashedPassword(), notNullValue());
		assertThat(test.getRole(), is(role));
		assertThat(test.getEmail(), is(email));
	}
	
	@Test
	public void testAuthenticateHashedCaseInsensitive() {
		String loginWithUpperCase = "TestLogin";		
		Account test = ctr.authenticateHashed(loginWithUpperCase, account.getHashedPassword());
		
		assertThat(test.getLogin(), is(login));
		assertThat(test.getHashedPassword(), notNullValue());
		assertThat(test.getRole(), is(role));
		assertThat(test.getEmail(), is(email));
	}
	
	@Test
	public void testAuthenticateHashedWrongHash() {
		assertThat(ctr.authenticateHashed(login, "wrongpasswordhash"), nullValue());
	}
	
	public void testAuthenticateHashedNullHash() {
		assertThat(ctr.authenticateHashed(login, null), nullValue());
	}
	
	@Test
	public void testAuthenticateHashedNullLogin() {
		assertThat(ctr.authenticateHashed(null, password), nullValue());
	}
	
	@Test
	public void testAuthenticateHashedUnknownLogin() {
		assertThat(ctr.authenticateHashed("notexistinglogin", password), nullValue());
	}
	
	@Test
	public void testAddNewsletterRecipientDuplicateEntry() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("already registered");
		String email = "email@host.de";
		RecipientDTO recipientDto = mock(RecipientDTO.class);
		when(recipientDto.getEmail()).thenReturn(email);
		when(recipientRepo.getByProperty("email", email)).thenReturn(mock(NewsletterRecipient.class));
		
		ctr.addNewsletterRecipient(recipientDto);
	}
	
	@Test
	public void testAddNewsletterRecipient() throws Exception {
		String email = "email@host.de";
		RecipientDTO recipientDto = mock(RecipientDTO.class);
		when(recipientDto.getEmail()).thenReturn(email);
		when(mailCtrl.newMimeMessage()).thenReturn(mock(Message.class));
				
		ctr.addNewsletterRecipient(recipientDto);
		
		ArgumentCaptor<NewsletterRecipient> recipientArgument = ArgumentCaptor.forClass(NewsletterRecipient.class);
		verify(recipientRepo).saveOrUpdate(recipientArgument.capture());
		assertThat(recipientArgument.getValue().getEmail(), is(email));
		assertThat(recipientArgument.getValue().getEntryDate(), notNullValue());
	}
	
	@Test
	public void testRemoveNewsletterRecipient() throws Exception {
		long id = 1;
		String email = "test@host.de";
		NewsletterRecipient recipient = mock(NewsletterRecipient.class);
		when(recipient.getEmail()).thenReturn(email);
		when(recipientRepo.getById(id)).thenReturn(recipient);
		
		ctr.removeNewsletterRecipient(id, email);
		
		verify(recipientRepo).delete(recipient);
	}
	
	@Test
	public void testIsAccountInRoleUserAsCompanyowner() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("companyowner");
				
		assertThat(ctr.isAccountInRole(account, "user"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleBusinessadminAsCompanyowner() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("companyowner");
		
		assertThat(ctr.isAccountInRole(account, "businessadmin"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleCockpituserAsCompanyowner() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("companyowner");
		
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleCompanyownerAsCompanyowner() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("companyowner");
		
		assertThat(ctr.isAccountInRole(account, "companyowner"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleCockpituserAsCockpituser() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("cockpituser");
		
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleBusinessadminAsCockpituser() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("cockpituser");
		
		assertThat(ctr.isAccountInRole(account, "businessadmin"), is(false));
	}
	
	@Test
	public void testIsAccountInRoleCompanyownerAsCockpituser() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("cockpituser");
		
		assertThat(ctr.isAccountInRole(account, "companyowner"), is(false));
	}
	
	@Test
	public void testIsAccountInRoleCompanyownerAsBusinessadmin() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("businessadmin");
		
		assertThat(ctr.isAccountInRole(account, "companyowner"), is(false));
	}
	
	@Test
	public void testIsAccountInRoleBusinessadminAsBusinessadmin() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("businessadmin");
		
		assertThat(ctr.isAccountInRole(account, "businessadmin"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleCockpituserAsBusinessadmin() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("businessadmin");
		
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(true));
	}
	
	@Test
	public void testIsAccountInRoleNullOrEmpty() throws Exception {
		account = mock(Account.class);
				
		assertThat(ctr.isAccountInRole(account, null), is(true));
		assertThat(ctr.isAccountInRole(account, ""), is(true));
	}
	
	@Test
	public void testIsAccountInRoleNullAccount() throws Exception {
		account = null;
				
		assertThat(ctr.isAccountInRole(account, "companyowner"), is(false));
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(false));
		assertThat(ctr.isAccountInRole(account, "businessadmin"), is(false));
		assertThat(ctr.isAccountInRole(account, "user"), is(false));
	}
	
	@Test
	public void testIsAccountManagingBusiness() throws Exception {
		account = mock(Account.class);
		long businessId = 1;
		ArrayList<Key<Business>> businessList = new ArrayList<Key<Business>>();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock( Key.class);
		when(businessKey.getId()).thenReturn(businessId);
		businessList.add(businessKey);
		when(account.getBusinesses()).thenReturn( businessList);
		
		assertThat(ctr.isAccountManagingBusiness(account, businessId), is(true));
		assertThat(ctr.isAccountManagingBusiness(account, businessId+1), is(false));
	}
	
	@Test
	public void testIsAccountManagingBusinessNullAccount() throws Exception {
		account = null;
		long businessId = 1;
		assertThat(ctr.isAccountManagingBusiness(account, businessId), is(false));
		assertThat(ctr.isAccountManagingBusiness(account, 0), is(false));
	}
	
	@Test
	public void testRegisterNewAccountLoginTooLong() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testaverylongloginnamewhichislongerthan30chars");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountLoginTooShort() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("tes");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountInvalidLogin3() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("test _user-1");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewAccount(data);
	}
	
	
	@Test
	public void testRegisterNewAccountInvalidLogin2() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("test_user-1$");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountInvalidLogin() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("TEST_user-1");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountPasswordTooShort() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("abc");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("password");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountInvalidPassword2() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("test1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("password");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountInvalidPassword() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("test");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(RegistrationException.class);
		thrown.expectMessage("password");
		
		ctr.registerNewAccount(data);
	}
	
	@Test
	public void testRegisterNewAccount() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("test!1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		account = ctr.registerNewAccount(data);
		assertThat(account.getEmail(), is(data.getEmail()));
		assertThat(account.getLogin(), is(data.getLogin()));
		assertThat(account.getName(), is(data.getName()));
		assertThat(BCrypt.checkpw(data.getPassword(), account.getHashedPassword()), is(true));
		assertThat(account.getCreationDate(), notNullValue());
		assertThat(account.isActive(), is(false));
		assertThat(account.getEmailConfirmationHash(), notNullValue());
		assertThat(account.isEmailConfirmed(), is(false));
		assertThat(account.getBusinesses(), nullValue());
		assertThat(account.getRole(), is(Role.COMPANYOWNER));
		assertThat(account.getId(), notNullValue());
		assertThat(account.getPhone(), is(data.getPhone()));
		ArgumentCaptor<Company> companyArg = ArgumentCaptor.forClass(Company.class);
		verify(companyRepo).saveOrUpdate(companyArg.capture());
		Company savedCompany = companyArg.getValue();
		
		assertThat(savedCompany.getName(), is(company.getName()));
		assertThat(savedCompany.getAddress(), is(company.getAddress()));
		assertThat(savedCompany.getCity(), is(company.getCity()));
		assertThat(savedCompany.getCountry(), is(company.getCountry()));
		assertThat(savedCompany.getPostcode(), is(company.getPostcode()));
		assertThat(savedCompany.getPhone(), is(company.getPhone()));
	}
	
	@Test
	public void testConfirmAccountEmail() throws Exception {
		account = ar.createAndSaveAccount("Test User", login, password,	email, role, rr.getAllKeys(), null, null, null, false, false);
		EmailConfirmationDTO data = new EmailConfirmationDTO();
		String confirmationToken = account.getEmailConfirmationHash();
		data.setConfirmationToken(confirmationToken);
		
		ctr.confirmAccountEmail(data);
	}
	
	@Test
	public void testConfirmAccountEmailNullConfirmationData() throws Exception {
		EmailConfirmationDTO data = null;
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("confirmationData");
		ctr.confirmAccountEmail(data);
	}
	
	@Test
	public void testConfirmAccountEmailInvalidToken() throws Exception {
		account = ar.createAndSaveAccount("Test User", login, password,	email, role, rr.getAllKeys(), null, null, null, false, false);
		EmailConfirmationDTO data = new EmailConfirmationDTO();
		String confirmationToken = account.getEmailConfirmationHash();
		data.setConfirmationToken("not"+confirmationToken);
		
		thrown.expect(ServiceException.class);
		thrown.expectMessage("token");
		ctr.confirmAccountEmail(data);
	}
	
	@Test
	public void testAuthenticateFacebook() throws Exception {
		String uid = "100000164823174";
		account = ar.createAndSaveAccount("Test User", login, password,	email, role, rr.getAllKeys(), null, null, uid, true, true);
		String accessToken = "test";
		JSONObject jsonMe = new JSONObject();
		jsonMe.put("id", uid);
		when(facebookService.getMe(accessToken)).thenReturn(jsonMe );
				
		ctr.authenticateFacebook(uid, accessToken );
	}
	
	@Test
	public void testAuthenticateFacebookUnknownUID() throws Exception {
		String uid = "100000164823174";
		String accessToken = "test";
		JSONObject jsonMe = new JSONObject();
		jsonMe.put("id", uid);
		jsonMe.put("name", "testuser");
		when(facebookService.getMe(accessToken)).thenReturn(jsonMe );
		
		thrown.expect(IllegalAccessException.class);
		ctr.authenticateFacebook(uid, accessToken );
	}
	
	@Test
	public void testAuthenticateFacebookInvalidAccessToken() throws Exception {
		String uid = "100000164823174";
		String accessToken = "test";
		JSONObject jsonMe = new JSONObject();
		jsonMe.put("id", uid);
		jsonMe.put("name", "testuser");
		when(facebookService.getMe(accessToken)).thenThrow(IllegalArgumentException.class);
		
		thrown.expect(IllegalAccessException.class);
		ctr.authenticateFacebook(uid, accessToken );
	}
	
	@Test
	public void testAuthenticateFacebookNullAccessToken() throws Exception {
		String uid = "100000164823174";
		String accessToken = null;
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("accessToken");
		ctr.authenticateFacebook(uid, accessToken );
	}
	
	@Test
	public void testAuthenticateFacebookNullUid() throws Exception {
		String uid = null;
		String accessToken = "test";
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("uid");
		ctr.authenticateFacebook(uid, accessToken );
	}
}
