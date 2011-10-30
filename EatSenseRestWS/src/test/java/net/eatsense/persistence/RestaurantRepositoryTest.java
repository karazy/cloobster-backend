package net.eatsense.persistence;

import static org.junit.Assert.*;
import junit.framework.Assert;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RestaurantRepositoryTest {
	

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    private Injector injector;
    private RestaurantRepository rr;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule());
		rr = injector.getInstance(RestaurantRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testSave() {
		
		Restaurant found = rr.findByKey(1l, Restaurant.class);
		assertNull(found);
		
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.save(r);
		
		found = rr.findByKey(1l, Restaurant.class);
		assertNotNull(found);
		
	}

	@Test
	public void testUpdate() {
		Restaurant found = rr.findByKey(1l, Restaurant.class);
		Assert.assertNull(found);
		
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
//		r.setId(1l);
		Key key = rr.save(r);
		
		found = rr.findByKey(key.getId(), Restaurant.class);
		assertEquals("Heidi und Paul", found.getName());
		found = null;
		
		r.setName("Vappiano");		
		rr.update(r);
		
		found = rr.findByKey(key.getId(), Restaurant.class);
		assertEquals("Vappiano", found.getName());
	}

	@Test
	public void testDelete() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.save(r);
		
		Restaurant found = rr.findByKey(1l, Restaurant.class);
		assertNotNull(found);
		
		rr.delete(r);
		
		found = rr.findByKey(1l, Restaurant.class);
		assertNull(found);
	}
	
	@Test
	public void testQueryBarcode() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		Area a = new Area();
		a.setId(2l);
		a.setName("Lounge");
		
		
		String bcode = "XYZ123";
		Barcode b = new Barcode();
		b.setId(3l);
		b.setCode(bcode);
		
		a.getBarcodes().add(b);
		r.getAreas().add(a);
		
		rr.save(r);
		
		Restaurant found = rr.findByKey(1l, Restaurant.class);
		assertNotNull(found);
		assertEquals(1, found.getAreas().size());
		
		found = rr.findByBarcode(bcode);
		assertEquals(bcode, found.getAreas().get(0).getBarcodes().get(0).getCode());
		
	}
	
	@Test
	public void testQueryArea() {
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		Area a = new Area();
		a.setId(2l);
		a.setName("Lounge");
		r.getAreas().add(a);
		
		rr.save(r);
		
		Restaurant found = rr.findByKey(1l, Restaurant.class);
		assertNotNull(found);
		assertEquals(1, found.getAreas().size());
		
		found = rr.findByArea(a.getName());
		assertEquals("Lounge", found.getAreas().get(0).getName());
		
	}
}