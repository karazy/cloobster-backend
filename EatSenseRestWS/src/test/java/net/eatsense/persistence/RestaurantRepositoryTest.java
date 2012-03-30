package net.eatsense.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import junit.framework.Assert;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Business;

import org.apache.bval.guice.ValidationModule;
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
    private BusinessRepository rr;
    private SpotRepository br;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		rr = injector.getInstance(BusinessRepository.class);
		br = injector.getInstance(SpotRepository.class);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testSave() {
		
		Business found = rr.getById(1l);
		assertNull(found);
		
		Business r = new Business();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.saveOrUpdate(r);
		
		found = rr.getById(1l);
		assertNotNull(found);
		
	}

	@Test
	public void testUpdate() {
		Business found = rr.getById(1l);
		Assert.assertNull(found);
		
		Business r = new Business();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
//		r.setId(1l);
		Key<Business> key = rr.saveOrUpdate(r);
		
		found = rr.getById(key.getId());
		assertEquals("Heidi und Paul", found.getName());
		found = null;
		
		r.setName("Vappiano");		
		rr.saveOrUpdate(r);
		
		found = rr.getById(key.getId());
		assertEquals("Vappiano", found.getName());
	}

	@Test
	public void testDelete() {
		Business r = new Business();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		r.setId(1l);
		rr.saveOrUpdate(r);
		
		Business found = rr.getById(1l);
		assertNotNull(found);
		
		rr.delete(r);
		
		found = rr.getById(1l);
		assertNull(found);
	}
	
	@Test
	public void testFindRestaurantByBarcode() {
		Business r = new Business();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		Key<Business> kR = rr.saveOrUpdate(r);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setBusiness(kR);
		
		Key<Spot> kB = br.saveOrUpdate(b); 
		
		Business found = rr.findByBarcode("b4rc0de");
		assertNotNull(found);
		assertEquals(kR.getId(), (long) found.getId());
	}
}