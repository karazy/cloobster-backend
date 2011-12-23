package net.eatsense.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import junit.framework.Assert;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Area;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Restaurant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class RestaurantRepositoryTest {
	

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    private Injector injector;
    private RestaurantRepository rr;
    private AreaRepository ar;
    private SpotRepository br;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule());
		rr = injector.getInstance(RestaurantRepository.class);
		ar = injector.getInstance(AreaRepository.class);
		br = injector.getInstance(SpotRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testSave() {
		
		Restaurant found = rr.findByKey(1l);
		assertNull(found);
		
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.saveOrUpdate(r);
		
		found = rr.findByKey(1l);
		assertNotNull(found);
		
	}

	@Test
	public void testUpdate() {
		Restaurant found = rr.findByKey(1l);
		Assert.assertNull(found);
		
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
//		r.setId(1l);
		Key<Restaurant> key = rr.saveOrUpdate(r);
		
		found = rr.findByKey(key.getId());
		assertEquals("Heidi und Paul", found.getName());
		found = null;
		
		r.setName("Vappiano");		
		rr.saveOrUpdate(r);
		
		found = rr.findByKey(key.getId());
		assertEquals("Vappiano", found.getName());
	}

	@Test
	public void testDelete() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.saveOrUpdate(r);
		
		Restaurant found = rr.findByKey(1l);
		assertNotNull(found);
		
		rr.delete(r);
		
		found = rr.findByKey(1l);
		assertNull(found);
	}
	
	@Test
	public void testFindRestaurantByArea() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");

		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Area a = new Area();
		a.setName("Lounge");
		a.setRestaurant(kR);

		
		Key<Area> kA = ar.saveOrUpdate(a);
		Area foundA = ar.getByKey(kR, kA.getId());
		
		Restaurant found = rr.findByKey(foundA.getRestaurant().getId());
		assertNotNull(found);
		assertEquals(kR.getId(), (long) found.getId());

	}
	
	@Test
	public void testFindRestaurantByBarcode() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		Key<Restaurant> kR = rr.saveOrUpdate(r);
		
		Area a = new Area();
		a.setName("Lounge");
		a.setRestaurant(kR);
		Key<Area> kA = ar.saveOrUpdate(a);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setArea(kA);
		Key<Spot> kB = br.saveOrUpdate(b); 
		
		Restaurant found = rr.findByBarcode("b4rc0de");
		assertNotNull(found);
		assertEquals(kR.getId(), (long) found.getId());
	}
}