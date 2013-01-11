package net.eatsense.persistence;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Business;

import org.apache.bval.guice.ValidationModule;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class DummyDataCreator {
	
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        
        private Injector injector;
        private LocationRepository rr;
        private SpotRepository br;
         
    	@Before
    	public void setUp() throws Exception {
    		helper.setUp();
    		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
    		rr = injector.getInstance(LocationRepository.class);
    		br = injector.getInstance(SpotRepository.class);
    	}
    	
    	@Test
    	public void createTestData() {
    		Business r = new Business();
    		r.setName("Zum flotten Hasen");
    		r.setDescription("Geiles Bio Burger Restaurant.");
    		Key<Business> kR = rr.saveOrUpdate(r);
    		
    		Spot b = new Spot();
    		b.setBarcode("b4rc0de");
    		b.setBusiness(kR);
    		Key<Spot> kB = br.saveOrUpdate(b);
    	}


}
