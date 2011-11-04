package net.eatsense.persistence;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

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
        private RestaurantRepository rr;
        private AreaRepository ar;
        private BarcodeRepository br;
        
    	@Before
    	public void setUp() throws Exception {
    		helper.setUp();
    		injector = Guice.createInjector(new EatSenseDomainModule());
    		rr = injector.getInstance(RestaurantRepository.class);
    		ar = injector.getInstance(AreaRepository.class);
    		br = injector.getInstance(BarcodeRepository.class);
    	}
    	
    	@Test
    	public void createTestData() {
    		Restaurant r = new Restaurant();
    		r.setName("Zum flotten Hasen");
    		r.setDescription("Geiles Bio Burger Restaurant.");
    		Key<Restaurant> kR = rr.saveOrUpdate(r);
    		
    		Area a = new Area();
    		a.setName("Hauptraum");
    		a.setRestaurant(kR);
    		Key<Area> kA = ar.saveOrUpdate(a);
    		
    		Barcode b = new Barcode();
    		b.setBarcode("b4rc0de");
    		b.setArea(kA);
    		Key<Barcode> kB = br.saveOrUpdate(b); 
    	}


}
