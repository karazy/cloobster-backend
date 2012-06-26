package net.eatsense.controller;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.*;


import java.util.Collection;

import javax.validation.Validator;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class MenuControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    private Injector injector;
    private MenuController ctr;
    private BusinessRepository rr;
    private MenuRepository mr;
    private ProductRepository pr;
    private ChoiceRepository cr;
    private DummyDataDumper ddd;
	
	private Business business;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(MenuController.class);
		rr = injector.getInstance(BusinessRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		
		ddd= injector.getInstance(DummyDataDumper.class);
		
		ddd.generateDummyBusinesses();
		business = rr.getByProperty("name", "Sergio");
	}
	
	public void newSetUp() throws Exception {
		mr = mock(MenuRepository.class);
		pr = mock(ProductRepository.class);
		cr = mock(ChoiceRepository.class);
		Transformer trans = mock(Transformer.class);
		Validator validator = injector.getInstance(Validator.class);
		
		ctr = new MenuController(mr, pr, cr, trans, validator);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	
	public MenuDTO getTestMenuData() {
		
		MenuDTO menuData = new MenuDTO();
		menuData.setDescription("test description");
		menuData.setOrder(1);
		menuData.setTitle("test title");
		return menuData ;
	}
	
	@Test
	public void testCreateMenu() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		when(business.getKey()).thenReturn(businessKey );
		MenuDTO testMenuData = getTestMenuData();
		Menu newMenu = new Menu();
		when(mr.newEntity()).thenReturn(newMenu );		
		
		ctr.createMenu(business, testMenuData);
		assertThat(newMenu.getBusiness(), is(businessKey));
		
		verify(mr).saveOrUpdate(newMenu);
	}
	
	@Test
	public void testUpdateMenuTitle() throws Exception {
		newSetUp();
		MenuDTO testMenuData = getTestMenuData();
		Menu menu = new Menu();
		menu.setDescription(testMenuData.getDescription());
		menu.setOrder(testMenuData.getOrder());
		menu.setTitle("another title");
		
		MenuDTO result = ctr.updateMenu(menu, testMenuData);
		
		verify(mr).saveOrUpdate(menu);
		assertThat(result.getTitle(), is(testMenuData.getTitle()));
	}
	
	@Test
	public void testUpdateMenuDescription() throws Exception {
		newSetUp();
		MenuDTO testMenuData = getTestMenuData();
		Menu menu = new Menu();
		menu.setDescription("another description");
		menu.setOrder(testMenuData.getOrder());
		menu.setTitle(testMenuData.getDescription());
		
		MenuDTO result = ctr.updateMenu(menu, testMenuData);
		
		verify(mr).saveOrUpdate(menu);
		assertThat(result.getDescription(), is(testMenuData.getDescription()));
	}
	
	@Test
	public void testUpdateMenuOrder() throws Exception {
		newSetUp();
		MenuDTO testMenuData = getTestMenuData();
		Menu menu = new Menu();
		menu.setDescription(testMenuData.getDescription());
		menu.setOrder(123);
		menu.setTitle(testMenuData.getTitle());
		
		MenuDTO result = ctr.updateMenu(menu, testMenuData);
		
		verify(mr).saveOrUpdate(menu);
		assertThat(result.getOrder(), is(testMenuData.getOrder()));
	}
	
	@Test
	public void testGetProductWithUnknownId() {
		thrown.expect(NotFoundException.class);
		ctr.getProductWithChoices(business, 123456l);
	}

	
	@Test
	public void testGetProduct() {
		
		Product product = pr.getByProperty("name", "Classic Burger");
		
		ProductDTO productDto = ctr.getProductWithChoices(business, product.getId());
		
		assertThat(productDto.getName(), is(product.getName()));
		assertThat(productDto.getChoices().size(), is(product.getChoices().size()));
	}
	
	@Test
	public void testGetMenus() {
		// retrieve all menus saved for this restaurant
		Collection<MenuDTO> menusdto = ctr.getMenusWithProducts(business);
		
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
					assertThat(p.getChoices().size(), is(4));
					
					for(ChoiceDTO c : p.getChoices())  {
						assertThat(c.getText(), anyOf(is("Wählen sie einen Gargrad:"), is("Extras"), is("Menü"), is("Menü Beilage")));
						assertThat(c.getOverridePrice(), is(ChoiceOverridePrice.NONE));
						
						if(c.getText().equals("Wählen sie einen Gargrad:")) {
							assertThat(c.getOptions().size(), is (3));
							
							for (ProductOption pO : c.getOptions()) {
								assertThat(pO.getName(), anyOf(is("Roh"), is("Medium"), is("Brikett")));
							}
						}
						
						if(c.getText().equals("Extras")) {
							assertThat(c.getOptions().size(), is (4) );
							
							for (ProductOption pO : c.getOptions()) {
								assertThat(pO.getName(), anyOf(is("Extra Käse"), is("Chili Sauce"),is("Salatgurken") , is("Ei")));
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
