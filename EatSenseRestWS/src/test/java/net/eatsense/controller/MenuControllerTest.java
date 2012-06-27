package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	public void testUpdateChoiceEmptyOptionsName() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.getOptions().get(0).setName("");
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceNullOptionsName() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.getOptions().get(0).setName(null);
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceEmptyOptions() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.setOptions(Collections.<ProductOption>emptyList());
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}

	@Test
	public void testUpdateChoiceNullOptions() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.setOptions(null);
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceNullText() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.setText(null);
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceEmptyText() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.setText("");
		Choice choice = new Choice();
		
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceInvalidPrice() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		
		ChoiceDTO data = getTestChoiceData();
		data.setPrice(-1f);
		Choice choice = new Choice();
		thrown.expect(ValidationException.class);
		ctr.updateChoice(choice, data);
	}
	
	@Test
	public void testUpdateChoiceProductExistinLink() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		// Change text of original entity, so it will updated with the test data.
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Product> productKey2 = mock(Key.class);
		Long productId2 = 2l;
		when(productKey2.getId()).thenReturn(productId2 );
		choice.setProduct(productKey2 );
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
	
		Product product = new Product();
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock (Key.class);
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		when(cr.getKey(businessKey, choice.getId())).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		//verify that the product didnot change
		assertThat(result.getProductId(), is(productId2));
		assertThat(choice.getProduct(), is(productKey2));
		
		// Check that we saved the product with the choice.
		verify(pr).saveOrUpdate(product);
		assertThat(product.getChoices(), hasItem(choiceKey));
		
		// Check that we never saved the choice.
		// Because we did not update any data.
		verify(cr, never()).saveOrUpdate(choice);
	}
	
	@Test
	public void testUpdateChoiceProduct() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		// Change text of original entity, so it will updated with the test data.
		choice.setText(data.getText());
		choice.setProduct(null);
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
	
		Product product = new Product();
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock (Key.class);
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getProductId(), is(data.getProductId()));
		assertThat(choice.getProduct(), is(productKey));
		
		// Check that we saved the product with the choice.
		verify(pr).saveOrUpdate(product);
		assertThat(product.getChoices(), hasItem(choiceKey));
	}
	
	@Test
	public void testUpdateChoicePrice() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		// Change text of original entity, so it will updated with the test data.
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(99f);
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getText(), is(data.getText()));
		assertThat(choice.getText(), is(data.getText()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceText() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		// Change text of original entity, so it will updated with the test data.
		choice.setText("another test text");
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getText(), is(data.getText()));
		assertThat(choice.getText(), is(data.getText()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceOverridePriceFlag() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		// Change override price of original entity, so it will updated with the test data.
		choice.setOverridePrice(ChoiceOverridePrice.OVERRIDE_FIXED_SUM);
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getOverridePrice(), is(data.getOverridePrice()));
		assertThat(choice.getOverridePrice(), is(data.getOverridePrice()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceOptions() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		List<ProductOption> options = new ArrayList<ProductOption>(data.getOptions());
		options.remove(0);
		// Change options of original entity, so it will updated with the test data.
		choice.setOptions(options );
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getOptions(), is(data.getOptions()));
		assertThat(choice.getOptions(), is(data.getOptions()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceMinOccurence() throws Exception {
		//TODO Remove after refactoring test class.
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		// Change min occurence of original entity, so it will updated with the test data.
		choice.setMinOccurence(1);
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getMinOccurence(), is(data.getMinOccurence()));
		assertThat(choice.getMinOccurence(), is(data.getMinOccurence()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceMaxOccurence() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(0);
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getMaxOccurence(), is(data.getMaxOccurence()));
		assertThat(choice.getMaxOccurence(), is(data.getMaxOccurence()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceIncluded() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(3);
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		assertThat(result.getIncluded(), is(data.getIncluded()));
		assertThat(choice.getIncludedChoices(), is(data.getIncluded()));
		
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
	}
	
	@Test
	public void testUpdateChoiceNoChanges() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.getKey(businessKey, choice.getId())).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ctr.updateChoice(choice, data);
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
		
		verify(cr, never()).saveOrUpdate(choice);
	}
	
	@Test
	public void testUpdateChoiceParent() throws Exception {
		newSetUp();
		business = mock(Business.class);
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock (Key.class);
		ChoiceDTO data = getTestChoiceData();
		when(business.getKey()).thenReturn(businessKey);
		Choice choice = new Choice();
		choice.setId(1l);
		choice.setBusiness(businessKey);
		choice.setIncludedChoices(data.getIncluded());
		choice.setMaxOccurence(data.getMaxOccurence());
		choice.setMinOccurence(data.getMinOccurence());
		choice.setOptions(data.getOptions());
		choice.setOverridePrice(data.getOverridePrice());
		choice.setText(data.getText());
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice = mock(Key.class);
		
		choice.setParentChoice(parentChoice );
		choice.setPrice(data.getPrice());
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock(Key.class);
		choice.setProduct(productKey );
		Product product = new Product();
		
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );
		
		@SuppressWarnings("unchecked")
		Key<Choice> parentChoice2 = mock(Key.class);
		when(cr.getKey(businessKey, data.getParent())).thenReturn(parentChoice2);
		
		@SuppressWarnings("unchecked")
		Key<Choice> choiceKey = mock(Key.class);
		// Add the Choice to the list of choices for the product.
		product.addChoice(choiceKey);
		when(cr.saveOrUpdate(choice)).thenReturn(choiceKey );
		// Reset dirty flag.
		choice.setDirty(false);
		
		ChoiceDTO result = ctr.updateChoice(choice, data);
		
		assertThat(result.getIncluded(), is(data.getIncluded()));
		assertThat(choice.getIncludedChoices(), is(data.getIncluded()));
		// Check that we did not touch the Product, because the Choice was already in the list.
		verify(pr, never()).saveOrUpdate(product);
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
		@SuppressWarnings("unchecked")
		Key<Product> productKey = mock (Key.class);
		when(productKey.getId()).thenReturn(data.getProductId());
		
		when(pr.getKey(businessKey, data.getProductId())).thenReturn(productKey );
		when(pr.getByKey(productKey)).thenReturn(product );

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
		data.setParent(3l);
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
