package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.controller.AccountController;
import net.eatsense.controller.MenuController;
import net.eatsense.domain.Account;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.NotFoundException;

public class AccountControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private AccountController ctr;
	    private RestaurantRepository rr;
	    private MenuRepository mr;
	    private ProductRepository pr;
	    private ChoiceRepository cr;
	    private DummyDataDumper ddd;

		private SpotRepository br;

		private String password;

		private String login;

		private String email;

		private String role;

		private Account account;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(AccountController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		
		ddd= injector.getInstance(DummyDataDumper.class);
		
		 password = "diesisteintestpasswort";
		 login = "testlogin";
		 email = "wurst@wurst.de";
		 role = "admin";
		account = ctr.createAndSaveAccount( login, password,
				email, role);
		
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void simpleAuthenticationTest() {
		
		//#1 Test correct password
		Account test = ctr.authenticate(login, password);
		
		assertThat(test.getLogin(), is(login));
		assertThat(test.getHashedPassword(), notNullValue());
		assertThat(test.getRole(), is(role));
		assertThat(test.getEmail(), is(email));

		//#2 Test wrong password

		test = ctr.authenticate(login, "wrongpassword");
		assertThat(test, nullValue());
		
	}
	
	@Test
	public void hashedAuthenticationTest() {
		Account test = ctr.authenticateHashed(login, account.getHashedPassword());
		assertThat(test, notNullValue());
	}
}
