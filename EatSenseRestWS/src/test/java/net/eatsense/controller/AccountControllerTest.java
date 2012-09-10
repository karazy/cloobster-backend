package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Role;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.CustomerProfileRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.BusinessAccountDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.CustomerAccountDTO;
import net.eatsense.representation.RecipientDTO;
import net.eatsense.representation.RegistrationDTO;
import net.eatsense.service.FacebookService;
import net.eatsense.validation.ValidationHelper;

import org.apache.bval.guice.ValidationModule;
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

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Injector injector;
	private AccountController ctr;
	
	@Mock
	private BusinessRepository rr;
	
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

	private ValidationHelper validator;

	private Company company;
	
	@Mock
	private Key<Company> companyKey;

	private String hashedPassword;
	
	@Mock
	private AccessTokenRepository accessTokenRepo;

	@Mock
	private EventBus eventBus;

	@Mock
	private CustomerProfileRepository profileRepo;

	@Mock
	private CheckInRepository checkInRepo;
	
	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(new EatSenseDomainModule(),
				new ValidationModule());
		validator = injector.getInstance(ValidationHelper.class);
		ctr = new AccountController(ar, rr, recipientRepo, companyRepo,
				validator, facebookService, imageCtrl, accessTokenRepo, eventBus, profileRepo, checkInRepo);

		password = "diesisteintestpasswort";
		login = "testlogin";
		email = "wurst@wurst.de";
		role = "admin";
		hashedPassword = "normallyareallystronghashedpassword";
		// TODO update to use restaurant id
		
		account = new Account();
		account.setActive(true);
		account.setEmail(email);
		account.setLogin(login);
		account.setRole(role);
		account.setEmailConfirmed(true);
		account.setHashedPassword(hashedPassword);
		account.setCompany(companyKey);
		when(ar.getByProperty("login", login)).thenReturn(account);
		when(ar.checkPassword(password, hashedPassword)).thenReturn(true);
		
		company = new Company();
		CompanyDTO data = getCompanyTestData();
		company.setAddress(data.getAddress());
		company.setCity(data.getCity());
		company.setCountry(data.getCountry());
		company.setName(data.getName());
		company.setPhone(data.getPhone());
		company.setPostcode(data.getPostcode());
		company.setUrl(data.getUrl());
		company.setDirty(false);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAuthenticate() {
		when(ar.checkPassword(password, hashedPassword)).thenReturn(true);
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
		thrown.expect(ValidationException.class);
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
		when(mailCtrl.newMimeMessage()).thenReturn(mock(MimeMessage.class));
				
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
		
		assertThat(ctr.isAccountInRole(account, "businessadmin"), is(false));
	}
	
	@Test
	public void testIsAccountInRoleCockpituserAsCompanyowner() throws Exception {
		account = mock(Account.class);
		when(account.getRole()).thenReturn("companyowner");
		
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(false));
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
		
		assertThat(ctr.isAccountInRole(account, "cockpituser"), is(false));
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewCompanyAccount(data);
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewCompanyAccount(data);
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewCompanyAccount(data);
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewCompanyAccount(data);
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("login");
		
		ctr.registerNewCompanyAccount(data);
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
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("password");
		
		ctr.registerNewCompanyAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountInvalidPassword() throws Exception {
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("testtest");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("password");
		
		ctr.registerNewCompanyAccount(data);
	}
	
	@Test
	public void testRegisterNewAccountValidPassword() throws Exception {
		Company newCompany = new Company();
		when(companyRepo.newEntity()).thenReturn(newCompany );
		Account newAccount = new Account();
		when(ar.newEntity()).thenReturn(newAccount);
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("testtest!");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);

		ctr.registerNewCompanyAccount(data);
	}
	
	@Test
	public void testRegisterNewAccount() throws Exception {
		Company newCompany = new  Company();
		when(companyRepo.newEntity()).thenReturn(newCompany );
		Account newAccount = new Account();
		when(ar.newEntity()).thenReturn(newAccount );
		
		RegistrationDTO data = new RegistrationDTO();
		data.setEmail("test@test.de");
		data.setLogin("testuser1");
		data.setName("Test Person");
		data.setPassword("testt1");
		CompanyDTO company = new CompanyDTO();
		company.setName("Test Company");
		company.setAddress("Street 1");
		company.setCity("City");
		company.setCountry("Country");
		company.setPostcode("12345");
		
		data.setCompany(company);
		
		when(ar.hashPassword(data.getPassword())).thenReturn(BCrypt.hashpw(data.getPassword(), BCrypt.gensalt()));
		
		account = ctr.registerNewCompanyAccount(data);
		
		verify(ar).saveOrUpdate(newAccount);
		
		assertThat(account.getEmail(), is(data.getEmail()));
		assertThat(account.getLogin(), is(data.getLogin()));
		assertThat(account.getName(), is(data.getName()));
		assertThat(BCrypt.checkpw(data.getPassword(), account.getHashedPassword()), is(true));
		assertThat(account.getCreationDate(), notNullValue());
		assertThat(account.isActive(), is(false));
		assertThat(account.isEmailConfirmed(), is(false));
		assertThat(account.getBusinesses(), nullValue());
		assertThat(account.getRole(), is(Role.COMPANYOWNER));
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
		account.setActive(false);
		account.setEmailConfirmed(false);
		@SuppressWarnings("unchecked")
		Key<Account> accountKey = mock(Key.class);
		when(ar.getByKey(accountKey )).thenReturn(account);
		
		ctr.confirmAccountEmail(accountKey);
		
		assertThat(account.isActive(), is(true));
		assertThat(account.isEmailConfirmed(), is(true));
		verify(ar).saveOrUpdate(account);
	}
	
	@Test
	public void testConfirmAccountEmailNullKey() throws Exception {
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("accountKey");
		ctr.confirmAccountEmail(null);
	}
	
	@Test
	public void testConfirmAccountEmailInvalidToken() throws Exception {
		@SuppressWarnings("unchecked")
		Key<Account> accountKey = mock(Key.class);
		when(ar.getByKey(accountKey )).thenThrow(NotFoundException.class);
		
		thrown.expect(net.eatsense.exceptions.NotFoundException.class);
		ctr.confirmAccountEmail(accountKey);
	}
	
	@Test
	public void testAuthenticateFacebook() throws Exception {
		String uid = "100000164823174";
		account.setFacebookUid(uid);
		when(ar.getByProperty("facebookUid", uid)).thenReturn(account);
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
	
	@Test
	public void testUpdateCompanyUrl() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setUrl("http://www.google.com");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getUrl(), is(companyData.getUrl()));
	}
	
	@Test
	public void testUpdateCompanyPostcode() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setPostcode("23456");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getPostcode(), is(companyData.getPostcode()));
	}
	
	@Test
	public void testUpdateCompanyInvalidPostcode() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setPostcode("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("postcode");
		
		ctr.updateCompany(company, companyData);
	}
	
	@Test
	public void testUpdateCompanyPhone() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setPhone("020-test2");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getPhone(), is(companyData.getPhone()));
	}
	
	@Test
	public void testUpdateCompanyInvalidName() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setName("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("name");
		
		ctr.updateCompany(company, companyData);
	}
	
	@Test
	public void testUpdateCompanyName() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setName("New Test Company");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getName(), is(companyData.getName()));
	}
	
	@Test
	public void testUpdateCompanyInvalidCountry() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setCountry("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("country");
		
		ctr.updateCompany(company, companyData);
	}
	
	@Test
	public void testUpdateCompanyCountry() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setCountry("US");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getCountry(), is(companyData.getCountry()));
	}
	
	@Test
	public void testUpdateCompanyInvalidCity() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setCity("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("city");
		
		ctr.updateCompany(company, companyData);
	}
	
	@Test
	public void testUpdateCompanyCity() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setCity("another city");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		assertThat(company.getCity(), is(companyData.getCity()));
	}
	
	@Test
	public void testUpdateCompanyAddress() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setAddress("new test street 2");
		ctr.updateCompany(company, companyData);
		verify(companyRepo).saveOrUpdate(company);
		
		assertThat(company.getAddress(), is(companyData.getAddress()));
	}
	
	@Test
	public void testUpdateCompanyInvalidAddress() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		companyData.setAddress("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("address");
		
		ctr.updateCompany(company, companyData);
	}
	
	@Test
	public void testUpdateCompanyNoChanges() throws Exception {
		CompanyDTO companyData = getCompanyTestData();
		ctr.updateCompany(company, companyData);
		verify(companyRepo, never()).saveOrUpdate(company);
	}	
		
	public CompanyDTO getCompanyTestData() {
		CompanyDTO data = new CompanyDTO();
		data.setAddress("test street 1");
		data.setCity("test city");
		data.setCountry("DE");
		data.setName("test company");
		data.setPhone("010101-TEST");
		data.setPostcode("12345");
		data.setUrl("http://www.karazy.net/");
		return data;
	}
	
	@Test
	public void testUpdateUserAccountUnknownBusiness() throws Exception {
		@SuppressWarnings("unchecked")
		List<Key<Business>> businesses = mock(List.class);
		account.setBusinesses(businesses );
		Account newAccount = new Account();
		newAccount.setCompany(companyKey);
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setLogin("testuser");
		accountData.setPassword("password");
		List<Long> businessIds = new ArrayList<Long>();
		long businessId1 = 1;
		long businessId2 = 2;
		businessIds.add(businessId2 );
		accountData.setBusinessIds(businessIds );
		@SuppressWarnings("unchecked")
		Key<Business> businessKey1 = mock(Key.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey2 = mock(Key.class);
		when(rr.getKey(businessId1)).thenReturn(businessKey1 );
		when(rr.getKey(businessId2)).thenReturn(businessKey2 );
		when(businesses.contains(businessKey1)).thenReturn(true);
		// We expect the method to thrown an exception if we try to add a
		// business id that was not managed by the owner account.
		thrown.expect(ValidationException.class);
		
		ctr.updateCompanyAccount(newAccount, account, accountData);
	}
	
	@Test
	public void testUpdateUserAccountLoginAlreadyExists() throws Exception {
		Account newAccount = new Account();
		newAccount.setRole(Role.COCKPITUSER);
		newAccount.setCompany(companyKey);
		
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setLogin("testuser");
		accountData.setPassword("password");
		
		@SuppressWarnings("unchecked")
		Key<Account> value = mock(Key.class);
		when(ar.getKeyByProperty("login", "testuser")).thenReturn(value );
		
		thrown.expect(ValidationException.class);
		ctr.updateCompanyAccount(newAccount, account, accountData);
	}
	
	@Test
	public void testUpdateCockpitUserAccount() throws Exception {
		@SuppressWarnings("unchecked")
		List<Key<Business>> businesses = mock(List.class);
		account.setBusinesses(businesses );
		Account newAccount = new Account();
		// We test the update of a cockpit account.
		newAccount.setRole(Role.COCKPITUSER);
		newAccount.setCompany(companyKey);
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setLogin("testuser");
		accountData.setPassword("password1");
		List<Long> businessIds = new ArrayList<Long>();
		long businessId1 = 1;
		businessIds.add(businessId1 );
		accountData.setBusinessIds(businessIds );
		@SuppressWarnings("unchecked")
		Key<Business> businessKey1 = mock(Key.class);
		when(rr.getKey(businessId1)).thenReturn(businessKey1 );
		when(businesses.contains(businessKey1)).thenReturn(true);
		when(ar.hashPassword(accountData.getPassword())).thenReturn("hashedpassword");
		
		ctr.updateCompanyAccount(newAccount, account, accountData);
		
		verify(ar).saveOrUpdate(newAccount);
		assertThat(newAccount.getLogin(),is(accountData.getLogin()));
		assertThat(newAccount.getHashedPassword(), is("hashedpassword"));
		assertThat(newAccount.getBusinesses(), hasItem(businessKey1));
		
	}
	
	@Test
	public void testAddAdminAccountWithDifferentCompany() throws Exception {
		Account newAccount = new Account();
		newAccount.setCompany(mock(Key.class));
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setEmail("test@cloobster.com");
		when(ar.getByProperty("email", accountData.getEmail())).thenReturn(newAccount);
		
		thrown.expect(ValidationException.class);

		ctr.createOrAddAdminAccount(account, accountData);
		
		verify(ar).saveOrUpdate(newAccount);
		
		assertThat(newAccount.getCompany(), is(account.getCompany()));
		assertThat(newAccount.getRole(), is(Role.BUSINESSADMIN));
	}
	
	@Test
	public void testAddAdminAccountWithNoCompany() throws Exception {
		Account newAccount = new Account();
		newAccount.setActive(true);
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setEmail("test@cloobster.com");
		when(ar.getByProperty("email", accountData.getEmail())).thenReturn(newAccount);
		
		ctr.createOrAddAdminAccount(account, accountData);
		
		verify(ar).saveOrUpdate(newAccount);
		
		assertThat(newAccount.getCompany(), is(account.getCompany()));
		assertThat(newAccount.getRole(), is(Role.BUSINESSADMIN));
	}
	
	@Test
	public void testCreateAdminAccount() throws Exception {
		Account newAccount = new Account();
		when(ar.newEntity()).thenReturn(newAccount);
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setEmail("test@cloobster.com");
		
		ctr.createOrAddAdminAccount(account, accountData);
		
		verify(ar).saveOrUpdate(newAccount);
		assertThat(newAccount.getEmail(), is(accountData.getEmail()));
		assertThat(newAccount.getCompany(), is(account.getCompany()));
		assertThat(newAccount.isActive(), is(false));
		assertThat(newAccount.isEmailConfirmed(), is(false));
		assertThat(newAccount.getRole(),is(Role.BUSINESSADMIN));
	}
	
	@Test
	public void testDeleteCockpituserAccount() throws Exception {
		Account newAccount = new Account();
		// We test the update of a cockpit account.
		newAccount.setRole(Role.COCKPITUSER);
		newAccount.setCompany(companyKey);
		
		ctr.deleteCompanyUserAccount(newAccount);
		verify(ar).delete(newAccount);
	}
	
	@Test
	public void testDeleteBusinessAdminAccount() throws Exception {
		Account newAccount = new Account();
		// We test the update of a cockpit account.
		newAccount.setRole(Role.BUSINESSADMIN);
		newAccount.setCompany(companyKey);
		List<Key<Business>> businesses = mock(List.class);
		newAccount.setBusinesses(businesses);
		
		ctr.deleteCompanyUserAccount(newAccount);
		
		verify(ar).saveOrUpdate(newAccount);
		assertThat(newAccount.getCompany(),nullValue());
		assertThat(newAccount.getBusinesses(), nullValue());
		verify(ar,never()).delete(newAccount);
	}
	
	@Test
	public void testCreateUserAccount() throws Exception {
		
		BusinessAccountDTO accountData = new BusinessAccountDTO();
		accountData.setLogin("testuser");
		accountData.setPassword("password!1");
		
		Account newAccount = new Account();
		when(ar.newEntity()).thenReturn(newAccount );
		
		ctr.createCockpitUserAccount(account, accountData );
		
		verify(ar).saveOrUpdate(newAccount);
		assertThat(newAccount.isActive(),is(true));
		assertThat(newAccount.getRole(), is(Role.COCKPITUSER));
		assertThat(newAccount.getCompany(), is(companyKey));
		assertThat(newAccount.isEmailConfirmed(), is(false));
	}
	
	@Test
	public void testUpdateAccount() throws Exception {
		
	}
	
	@Test
	public void testRegisterNewCustomerAccount() throws Exception {
		Account newAccount = new Account();
		CustomerProfile newProfile = new CustomerProfile();
		@SuppressWarnings("unchecked")
		Key<CustomerProfile> profileKey = mock(Key.class);
		CustomerAccountDTO accountData = getTestCustomerAccountData();
		String pwHash = "passwordhash";
		
		when(ar.newEntity()).thenReturn(newAccount);
		
		when(ar.hashPassword(accountData.getPassword())).thenReturn(pwHash);
		when(profileRepo.newEntity()).thenReturn(newProfile);
		
		when(profileRepo.saveOrUpdate(newProfile)).thenReturn(profileKey);
				
		ctr.registerNewCustomerAccount(accountData);
		
		verify(ar).saveOrUpdate(newAccount);
		
		assertThat(newAccount.getActiveCheckIn(), nullValue());
		assertThat(newAccount.getBusinesses(), nullValue());
		assertThat(newAccount.getCompany(), nullValue());
		assertThat("creationDate is before now", newAccount.getCreationDate(), lessThanOrEqualTo(new Date()));
		assertThat(newAccount.getCustomerProfile(), is(profileKey));
		assertThat(newAccount.getEmail(), is(accountData.getEmail()));
		assertThat(newAccount.getFacebookUid(), is(nullValue()));
		assertThat(newAccount.getFailedLoginAttempts(), is(0));
		assertThat(newAccount.getHashedPassword(), is(pwHash));
		assertThat(newAccount.getLastFailedLogin(), nullValue());
		assertThat(newAccount.getLogin(), is(accountData.getLogin()));
		assertThat(newAccount.getName(), is(accountData.getName()));
		assertThat(newAccount.getNewEmail(), nullValue());
		assertThat(newAccount.getPhone(), nullValue());
		assertThat(newAccount.getRole(), is(Role.USER));
	}
	
	private CustomerAccountDTO getTestCustomerAccountData() {
		CustomerAccountDTO data = new CustomerAccountDTO();
		
		data.setEmail("test@example.com");
		data.setName("Test");
		data.setLogin("test");
		data.setPassword("passw0rd");
		
		return data;
	}
}
