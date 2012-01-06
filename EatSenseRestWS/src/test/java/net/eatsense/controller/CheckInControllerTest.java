package net.eatsense.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class CheckInControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private CheckInController ctr;
	    private RestaurantRepository rr;
	    private SpotRepository br;
	    private CheckInRepository cr;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		br = injector.getInstance(SpotRepository.class);
		cr = injector.getInstance(CheckInRepository.class);
		
		//create necessary data in datastore
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setRestaurant(kR);
		Key<Spot> kB = br.saveOrUpdate(b); 
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testCheckInProcessStandardCheckIn() {		
		CheckInDTO data = ctr.checkInIntent("b4rc0de");
		assertEquals("Heidi und Paul", data.getRestaurantName());
		assertNotNull(data.getUserId());
		assertNotNull(data.getNickname());
		assertNotNull(data.getRestaurantId());
		data.setNickname("FakeNik");
		ctr.checkIn(data.getUserId(), data);
		CheckIn chkin = cr.getByProperty("userId", data.getUserId());
		assertEquals(CheckInStatus.CHECKEDIN, chkin.getStatus());
		assertEquals("FakeNik", chkin.getNickname());
		
	}
	
	@Test
	public void testCheckInProcessValidationError() {		
		CheckInDTO data = ctr.checkInIntent("b4rc0de");
		assertEquals("Heidi und Paul", data.getRestaurantName());
		assertNotNull(data.getUserId());
		assertNotNull(data.getNickname());
		assertNotNull(data.getRestaurantId());
		
		//Part1: set nickname too short for this test
		data.setNickname("Fa");
		CheckInDTO data2 =  ctr.checkIn(data.getUserId(), data);
		// validation error should happen
		assertEquals(CheckInStatus.VALIDATION_ERROR.toString() ,data2.getStatus() );
		
		CheckIn chkin = cr.getByProperty("userId", data.getUserId());
		// status should still be intent
		assertEquals(CheckInStatus.INTENT, chkin.getStatus());
		// no nickname should have been written
		assertEquals(null , chkin.getNickname());
		
		//Part2: set nickname too long for this test
		data.setNickname("Fa123456789012345678901234567890");
		data2 =  ctr.checkIn(data.getUserId(), data);
		// validation error should happen
		assertEquals(CheckInStatus.VALIDATION_ERROR.toString() ,data2.getStatus() );
				
		chkin = cr.getByProperty("userId", data.getUserId());
		// status should still be intent
		assertEquals(CheckInStatus.INTENT, chkin.getStatus());
		// no nickname should have been written
		assertEquals(null , chkin.getNickname());
		
		// Part3: set nickname right now
		data.setNickname("FakeNik");
		data2 = ctr.checkIn(data.getUserId(), data);
		assertEquals(CheckInStatus.CHECKEDIN.toString() ,data2.getStatus() );
		
		chkin = cr.getByProperty("userId", data.getUserId());
		assertEquals(CheckInStatus.CHECKEDIN, chkin.getStatus());
		assertEquals("FakeNik", chkin.getNickname());
	}
	
	
	@Test
	public void testCheckInProcessLinkedCheckIn() {
		//checkIn user 1
		CheckInDTO data = ctr.checkInIntent("b4rc0de");
		data.setNickname("Peter Pan");
		ctr.checkIn(data.getUserId(), data);
		//checkIn user 2
		CheckInDTO data2 = ctr.checkInIntent("b4rc0de");
		data2.setNickname("Papa Schlumpf");
		String returnVal = ctr.checkIn(data2.getUserId(), data2).getStatus();
		CheckIn chkin = cr.getByProperty("userId", data.getUserId());
		//if another user is checked in youReNotAlone is returned
		assertEquals("YOUARENOTALONE", returnVal);
		//load users at same spot
		List<User> users = ctr.getUsersAtSpot(data.getUserId());
		assertEquals(1, users.size());
		//the second checked in user should be Papa Schlumpf
		assertEquals("Papa Schlumpf", users.get( 0 ).getNickname());
		//link user
		ctr.linkToUser(data.getUserId(), data2.getUserId());
		//check if user is linked
		chkin = cr.getByProperty("userId", data.getUserId());
		assertEquals(data2.getUserId(), chkin.getLinkedUserId());
	}

}
