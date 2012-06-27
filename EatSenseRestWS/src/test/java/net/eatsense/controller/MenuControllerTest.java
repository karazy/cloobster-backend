package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.ProductRepository;
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
	
	@Test
	public void testCreateChoice() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		
		when(business.getKey()).thenReturn(businessKey);
		Product product = new Product();
		when(pr.getById(businessKey, data.getProductId())).thenReturn(product );
		Choice newChoice = new Choice();
		when(cr.newEntity()).thenReturn(newChoice );
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		when(cr.saveOrUpdate(newChoice)).thenReturn(choiceKey );

		ctr.createChoice(business, data);
		
		assertThat(product.getChoices(), hasItem(choiceKey));
	}
	
	public ChoiceDTO getTestChoiceData() {
		ChoiceDTO data =  new ChoiceDTO();
		data.setParent(1l);
		data.setIncluded(1);
		data.setMaxOccurence(1);
		data.setMinOccurence(0);
		List<ProductOption> options = new ArrayList<ProductOption>();
		options.add( new ProductOption("Option 1", 1f));
		options.add( new ProductOption("Option 2", 2f));
		data.setOptions(options );
		data.setOverridePrice(ChoiceOverridePrice.NONE);
		data.setPrice(0f);
		data.setProductId(1l);
		data.setText("test text");
		return data;
	}
	
	public MenuDTO getTestMenuData() {
		
		MenuDTO menuData = new MenuDTO();
		menuData.setDescription("test description");
		menuData.setOrder(1);
		menuData.setTitle("test title");
		return menuData ;
	}
	
	@Test
	public void testUpdateProductWithNegativePrice() throws Exception {
		newSetUp();
			
		ProductDTO testProductData = getTestProductData();
		Product product = new Product();
		testProductData.setPrice(-1f);
		
		thrown.expect(ValidationException.class);
				
		ctr.updateProduct(product, testProductData);
	}
	
	@Test
	public void testUpdateProductWithNullPrice() throws Exception {
		newSetUp();
			
		ProductDTO testProductData = getTestProductData();
		Product product = new Product();
		testProductData.setPrice(null);
		
		thrown.expect(ValidationException.class);
				
		ctr.updateProduct(product, testProductData);
	}

	
	@Test
	public void testUpdateProductWithEmptyName() throws Exception {
		newSetUp();
			
		ProductDTO testProductData = getTestProductData();
		Product product = new Product();
		testProductData.setName("");
		
		thrown.expect(ValidationException.class);
				
		ctr.updateProduct(product, testProductData);
	}
	
	@Test
	public void testUpdateProductWithNullName() throws Exception {
		newSetUp();
			
		ProductDTO testProductData = getTestProductData();
		Product product = new Product();
		testProductData.setName(null);
		
		thrown.expect(ValidationException.class);
				
		ctr.updateProduct(product, testProductData);
	}

	@Test
	public void testUpdateProductLongDesc() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setName(testProductData.getName());
		product.setOrder(testProductData.getOrder());
		product.setPrice(testProductData.getPrice());
		product.setMenu(menuKey);
		product.setShortDesc(testProductData.getShortDesc());
		product.setLongDesc("another long desc");
		product.setDirty(false);
				
		ProductDTO result = ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getLongDesc(), is(testProductData.getLongDesc()));
		assertThat(result.getLongDesc(), is(testProductData.getLongDesc()));
	}
	
	@Test
	public void testUpdateProductShortDesc() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setLongDesc(testProductData.getLongDesc());
		product.setName(testProductData.getName());
		product.setOrder(testProductData.getOrder());
		product.setPrice(testProductData.getPrice());
		product.setMenu(menuKey);
		product.setShortDesc("another short desc");
		product.setDirty(false);
				
		ProductDTO result = ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getShortDesc(), is(testProductData.getShortDesc()));
		assertThat(result.getShortDesc(), is(testProductData.getShortDesc()));
	}
	
	@Test
	public void testUpdateProductMenu() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey2 = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setLongDesc(testProductData.getLongDesc());
		product.setName(testProductData.getName());
		product.setOrder(testProductData.getOrder());
		product.setPrice(testProductData.getPrice());
		product.setMenu(menuKey2);
		product.setShortDesc(testProductData.getShortDesc());
		product.setDirty(false);
				
		ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getMenu(), is(menuKey));
	}
	
	
	@Test
	public void testUpdateProductPrice() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setLongDesc(testProductData.getLongDesc());
		product.setName(testProductData.getName());
		product.setOrder(testProductData.getOrder());
		product.setPrice(999.9f);
		product.setMenu(menuKey);
		product.setShortDesc(testProductData.getShortDesc());
		product.setDirty(false);
				
		ProductDTO result = ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getPrice(), is(testProductData.getPrice()));
		assertThat(result.getPrice(), is(testProductData.getPrice()));
	}
	
	@Test
	public void testUpdateProductOrder() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setLongDesc(testProductData.getLongDesc());
		product.setName(testProductData.getName());
		product.setOrder(999);
		product.setPrice(testProductData.getPrice());
		product.setMenu(menuKey);
		product.setShortDesc(testProductData.getShortDesc());
		product.setDirty(false);
				
		ProductDTO result = ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getOrder(), is(testProductData.getOrder()));
		assertThat(result.getOrder(), is(testProductData.getOrder()));
	}
	
	@Test
	public void testUpdateProductName() throws Exception {
		newSetUp();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		@SuppressWarnings("unchecked")
		Key<Menu> menuKey = mock (Key.class);
			
		ProductDTO testProductData = getTestProductData();
		when(mr.getKey(businessKey, testProductData.getMenuId())).thenReturn(menuKey);
		
		Product product = new Product();
		product.setBusiness(businessKey);
		product.setLongDesc(testProductData.getLongDesc());
		product.setName("another name");
		product.setOrder(testProductData.getOrder());
		product.setPrice(testProductData.getPrice());
		product.setMenu(menuKey);
		product.setShortDesc(testProductData.getShortDesc());
		product.setDirty(false);
				
		ProductDTO result = ctr.updateProduct(product, testProductData);
		verify(pr).saveOrUpdate(product);
		
		assertThat(product.getName(), is(testProductData.getName()));
		assertThat(result.getName(), is(testProductData.getName()));
	}
	
	@Test
	public void testCreateProduct() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		when(business.getKey()).thenReturn(businessKey );

		ProductDTO testProductData = getTestProductData();
		Product newProduct = new Product();
		when(pr.newEntity()).thenReturn(newProduct );		
		
		ctr.createProduct(business, testProductData);
		assertThat(newProduct.getBusiness(), is(businessKey));
		
		verify(pr).saveOrUpdate(newProduct);
	}
	
	private ProductDTO getTestProductData() {
		ProductDTO data = new ProductDTO();
		data.setLongDesc("test long description");
		data.setShortDesc("test short desc");
		data.setMenuId(1l);
		data.setName("test name");
		data.setOrder(1);
		data.setPrice(1.0f);
		data.setShortDesc("test short description");
		return data;
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
		menu.setDirty(false);
		
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
		menu.setDirty(false);
		
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
		menu.setDirty(false);
		
		MenuDTO result = ctr.updateMenu(menu, testMenuData);
		
		verify(mr).saveOrUpdate(menu);
		assertThat(result.getOrder(), is(testMenuData.getOrder()));
	}
	
	@Test
	public void testGetProductWithUnknownId() {
		thrown.expect(NotFoundException.class);
		ctr.getProduct(business.getKey(), 123456l);
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
