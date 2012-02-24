package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
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
import net.eatsense.representation.ErrorDTO;
import net.eatsense.representation.SpotDTO;

import org.apache.bval.guice.ValidationModule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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

		private ObjectMapper mapper;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		br = injector.getInstance(SpotRepository.class);
		cr = injector.getInstance(CheckInRepository.class);
		mapper = injector.getInstance(ObjectMapper.class);
		//create necessary data in datastore
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setName("Tisch 1");
		b.setRestaurant(kR);
		Key<Spot> kB = br.saveOrUpdate(b); 
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testCheckInProcessStandardCheckIn() {	
		SpotDTO spot = ctr.getSpotInformation("b4rc0de");
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId("b4rc0de");
		
		assertEquals("Heidi und Paul", spot.getRestaurant());
		assertNotNull(spot.getBarcode());
		assertNotNull(spot.getName());
		assertNotNull(spot.getRestaurantId());
		
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
		assertThat(checkIn, notNullValue());
		assertThat(checkIn.getUserId(), notNullValue());
		
		CheckIn chkin = cr.getByProperty("userId", checkIn.getUserId());
		assertEquals(CheckInStatus.CHECKEDIN, chkin.getStatus());
		assertEquals("FakeNik", chkin.getNickname());
		
	}
	
	@Test
	public void testCheckInProcessValidationError() throws JsonParseException, JsonMappingException, IOException {		
		SpotDTO spot = ctr.getSpotInformation("b4rc0de");
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setStatus(CheckInStatus.INTENT);
		
		assertEquals("Heidi und Paul", spot.getRestaurant());
		assertNotNull(spot.getBarcode());
		assertNotNull(spot.getName());
		assertNotNull(spot.getRestaurantId());
		
		//Part1: set nickname too short for this test
		checkInData.setNickname("Fa");

		try {
			checkInData =  ctr.createCheckIn( checkInData);
		} catch (RuntimeException e) {
			ErrorDTO error = mapper.readValue(e.getMessage(), ErrorDTO.class);
			
			assertThat(error.getErrorKey(), is("checkInErrorNickname"));//Nickname"));
		}
	
		CheckIn checkIn = cr.getByProperty("nickname", checkInData.getNickname());
		
				
		// checkin should not be saved
		assertThat(checkIn, nullValue());
		
		//Part2: set nickname too long for this test
		checkInData.setNickname("Fa123456789012345678901234567890");

		// validation error should happen

		try {
			checkInData =  ctr.createCheckIn( checkInData);
		} catch (RuntimeException e) {
			ErrorDTO error = mapper.readValue(e.getMessage(), ErrorDTO.class);
			assertThat(error.getErrorKey(), is("checkInErrorNickname"));
		}
	
		checkIn = cr.getByProperty("nickname", checkInData.getNickname());
		
				
		// checkin should not be saved
		assertThat(checkIn, nullValue());
		
		// Part3: set nickname right now
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = ctr.createCheckIn(checkInData);
		assertThat(checkInData.getUserId(), notNullValue());
		
		checkIn = cr.getByProperty("userId", checkInData.getUserId());
		assertThat(checkIn, notNullValue());
		assertThat(checkIn.getStatus(), is(CheckInStatus.CHECKEDIN));
		assertThat(checkIn.getNickname(), is(checkInData.getNickname()));

	}
	
	
	@Test
	public void testCheckInProcessLinkedCheckIn() {
		SpotDTO spot = ctr.getSpotInformation("b4rc0de");
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setNickname("Peter Pan");
		
		assertEquals("Heidi und Paul", spot.getRestaurant());
		assertNotNull(spot.getBarcode());
		assertNotNull(spot.getName());
		assertNotNull(spot.getRestaurantId());
		
		checkInData = ctr.createCheckIn(checkInData);
		assertThat(checkInData, notNullValue());
		assertThat(checkInData.getUserId(), notNullValue());
		
		CheckInDTO checkInData2 = new CheckInDTO();
		checkInData2.setSpotId("b4rc0de");
		checkInData2.setStatus(CheckInStatus.INTENT);
		checkInData2.setNickname("Papa Schlumpf");
		
		checkInData2 = ctr.createCheckIn(checkInData2);
		assertThat(checkInData2, notNullValue());
		assertThat(checkInData2.getUserId(), notNullValue());
		
		//load users at same spot
		List<User> users = ctr.getUsersAtSpot(checkInData.getSpotId(), checkInData.getUserId());
		
		assertThat(users.size(), is(1));
		//the second checked in user should be Papa Schlumpf
		assertThat(users.get( 0 ).getNickname(), is(checkInData2.getNickname()) );
		//link user
		checkInData.setLinkedCheckInId(checkInData2.getUserId());
		CheckInDTO result = ctr.updateCheckIn(checkInData.getUserId(), checkInData);
		assertThat(result, notNullValue());
		
		//check if user is linked
		CheckIn checkIn = cr.getByProperty("userId", checkInData.getUserId());
		assertThat(checkIn.getLinkedUserId(), is(checkInData2.getUserId()));
	}

}
