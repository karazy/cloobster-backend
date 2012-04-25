package net.eatsense.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;

import org.apache.bval.guice.ValidationModule;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class ChannelControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private ChannelController ctr;
	    private BusinessRepository rr;
	    private SpotRepository br;
	    private CheckInRepository cr;

		private ObjectMapper mapper;

		private Business business;

		private CheckInController checkInCtrl;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(ChannelController.class);
		checkInCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(BusinessRepository.class);
		br = injector.getInstance(SpotRepository.class);
		cr = injector.getInstance(CheckInRepository.class);
		mapper = injector.getInstance(ObjectMapper.class);
		//create necessary data in datastore
		business = new Business();
		business.setName("Heidi und Paul");
		business.setDescription("Geiles Bio Burger Restaurant.");
		Key<Business> kR = rr.saveOrUpdate(business);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setName("Tisch 1");
		b.setBusiness(kR);
		Key<Spot> kB = br.saveOrUpdate(b); 
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	
	
	@Test
	public void testRequestChannelToken() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		String result;
		//#2.1 Request token with null id ...
		try {
			result = ctr.createCustomerChannel(null);
		} catch (Exception e) {
			assertThat(e, instanceOf(NullPointerException.class));
			
		}
		
		//#3.1 Request token with valid uid ...
		result = ctr.createCustomerChannel(checkIn);
		assertThat(result, notNullValue());
		assertThat(result.length(), is(greaterThan(8)));
	
		//#3.2 Request another token with the same uid, should create a new token ...
		String newResult = ctr.createCustomerChannel(checkIn);
		assertThat(newResult, is(not(result)));
	}
}
