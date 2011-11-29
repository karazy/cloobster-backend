package net.eatsense.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BarcodeRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckInDTO;

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
	    private AreaRepository ar;
	    private BarcodeRepository br;
	    private CheckInRepository cr;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule());
		ctr = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		ar = injector.getInstance(AreaRepository.class);
		br = injector.getInstance(BarcodeRepository.class);
		cr = injector.getInstance(CheckInRepository.class);
		
		//create necessary data in datastore
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Area a = new Area();
		a.setName("Lounge");
		a.setRestaurant(kR);
		Key<Area> kA = ar.saveOrUpdate(a);
		
		Barcode b = new Barcode();
		b.setBarcode("b4rc0de");
		b.setArea(kA);
		Key<Barcode> kB = br.saveOrUpdate(b); 
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testCheckInProcess() {
		CheckInDTO data = ctr.checkInIntent("b4rc0de");
		assertEquals("Heidi und Paul", data.getRestaurantName());
		assertNotNull(data.getUserId());
		ctr.checkIn(data.getUserId());
		CheckIn chkin = cr.getByProperty("userId", data.getUserId());
		assertEquals(CheckInStatus.CHECKEDIN, chkin.getStatus());		
		
	}

}
