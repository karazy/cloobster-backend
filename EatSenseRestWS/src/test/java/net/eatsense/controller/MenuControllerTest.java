package net.eatsense.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class MenuControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private MenuController ctr;
	    private RestaurantRepository rr;
	    private MenuRepository mr;
	    private ProductRepository pr;
	    private CheckInRepository cr;
	    
	    private Key<Restaurant> kR;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule());
		ctr = injector.getInstance(MenuController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		
		//create necessary data in datastore
		Restaurant r = new Restaurant();
		r.setName("Heidi und Paul");
		r.setDescription("Geiles Bio Burger Restaurant.");
		kR = rr.saveOrUpdate(r);
		
		Menu m = new Menu();
		m.setTitle("Burger");
		m.setRestaurant(kR);
		m.setDescription("geile Burger aus aller Welt.");
		Key<Menu> kM = mr.saveOrUpdate(m);
		
		Product p = new Product();
		p.setName("Classic Burger");
		p.setShortDesc("Burger mit Rindfleischpatty und Salat");
		p.setPrice(8.0f);
		p.setMenu(kM);
		
		pr.saveOrUpdate(p);
		
		p = new Product();
		p.setName("Veggie Burger");
		p.setShortDesc("Vegetarischer Burger mit Gemüste Patty und Käse");
		p.setPrice(8.0f);
		p.setMenu(kM);
		
		pr.saveOrUpdate(p);
		
		
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testGetMenus() {
		// retrieve all menus saved for this restaurant
		Collection<MenuDTO> menusdto = ctr.getMenus(kR.getId());
		
		// check if we have one menu
		assertEquals(1 , menusdto.size() );
		
		Collection<ProductDTO> products = menusdto.iterator().next().getProducts();
		// check if we have two products in this menu
		assertEquals(2, products.size());
		
		// check if the price is 8 for any product
		assertEquals(8.0, (double)products.iterator().next().getPrice(), 0);
		assertEquals(8.0, (double)products.iterator().next().getPrice(), 0);
		
	}

}
