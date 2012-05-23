package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.mail.Message;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Account;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.NewsletterRecipientRepository;
import net.eatsense.representation.RecipientDTO;

import org.apache.bval.guice.ValidationModule;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
	
	private AccountRepository ar;
	
	@Mock
	private NewsletterRecipientRepository recipientRepo;
	
	@Mock
	private MailController mailCtrl;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		channelController = mock(ChannelController.class);
		rr = injector.getInstance(BusinessRepository.class);
		ar = injector.getInstance(AccountRepository.class);
		ValidatorFactory avf =
	            Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory();
		ctr = new AccountController(ar, rr,recipientRepo, channelController, avf.getValidator());
		
		
		 password = "diesisteintestpasswort";
		 login = "testlogin";
		 email = "wurst@wurst.de";
		 role = "admin";
		 //TODO update to use restaurant id
		account = ar.createAndSaveAccount( login, password,
				email, role, rr.getAllKeys());		 
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
}
