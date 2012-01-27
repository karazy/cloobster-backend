package net.eatsense.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.*;
import static org.hamcrest.CoreMatchers.*;


import java.util.Collection;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.ChoiceOverridePrice;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MenuControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private MenuController ctr;
	    private RestaurantRepository rr;
	    private MenuRepository mr;
	    private ProductRepository pr;
	    private ChoiceRepository cr;
	    private DummyDataDumper ddd;

		private SpotRepository br;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(MenuController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		
		ddd= new DummyDataDumper(rr, br, mr, pr, cr);
		
		ddd.generateDummyRestaurants();
		
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testGetMenus() {
		// retrieve all menus saved for this restaurant
		Restaurant restaurant = rr.getByProperty("name", "Sergio");
		
		Collection<MenuDTO> menusdto = ctr.getMenus(restaurant.getId());
		
		// check if we have three menus
		assertEquals(3 , menusdto.size() );
		
		for(MenuDTO menu : menusdto )  {
			// check if all expected menus are present
			assertThat(menu.getTitle(), anyOf(is("Hauptgerichte"), is("Beilagen"), is("Getränke") ));
			
			// check "Hauptgerichte" menu contents
			if(menu.getTitle().equals("Hauptgerichte") ) {
				
				assertThat(menu.getProducts().size(), is(1));
				
				for(ProductDTO p : menu.getProducts()) {
					assertThat(p.getName(), is("Classic Burger") );
					assertThat(p.getChoices().size(), is(2));
					
					for(ChoiceDTO c : p.getChoices())  {
						assertThat(c.getText(), anyOf(is("Wählen sie einen Gargrad:"), is("Beilagen:")));
						assertThat(c.getOverridePrice(), is(ChoiceOverridePrice.NONE));
						
						if(c.getText().equals("Wählen sie einen Gargrad:")) {
							assertThat(c.getOptions().size(), is (3));
							
							for (ProductOption pO : c.getOptions()) {
								assertThat(pO.getName(), anyOf(is("Roh"), is("Medium"), is("Brikett")));
							}
						}
						
						if(c.getText().equals("Beilagen")) {
							assertThat(c.getOptions().size(), is (2) );
							
							for (ProductOption pO : c.getOptions()) {
								assertThat(pO.getName(), anyOf(is("Pommes Frites"), is("Krautsalat")));
							}
						}
						
					}
					
				}
			}
			
			// check "Getränke" menu contents
			if(menu.getTitle().equals("Getränke") ) {
				
				assertThat(menu.getProducts().size(), is(2));
			}
				
		}
	}

}
