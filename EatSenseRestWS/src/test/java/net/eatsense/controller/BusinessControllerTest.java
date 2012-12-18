package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Menu;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.configuration.Configuration;
import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.AreaDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.RequestDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.SpotStatusDTO;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class BusinessControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private Injector injector;
	private CheckInController checkinCtrl;
	private LocationRepository rr;
	private MenuRepository mr;
	private ProductRepository pr;
	private ChoiceRepository cr;
	private OrderRepository or;
	private DummyDataDumper ddd;

	private SpotRepository br;

	private Transformer transform;

	private OrderChoiceRepository ocr;

	private CheckInDTO checkInData;

	private LocationController businessCtrl;

	private CheckIn checkIn;

	private Location business;

	private AccountRepository accountRepo;

	@Mock
	private ImagesService imagesService;

	@Mock
	private BlobstoreService blobstoreService;

	@Mock
	private EventBus eventBus;

	private Validator validator;

	private RequestRepository requestRepo;

	private CheckInRepository checkInrepo;

	@Mock
	private ImageController imageController;
	@Mock
	private AreaRepository areaRepo;
	
	@Mock
	private Provider<Configuration> configProvider;
	
	@Mock
	private FeedbackFormRepository feedbackRepo;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		
		
		checkinCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(LocationRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		or = injector.getInstance(OrderRepository.class);
		ocr = injector.getInstance(OrderChoiceRepository.class);
		accountRepo = injector.getInstance(AccountRepository.class);
		transform = injector.getInstance(Transformer.class);
		validator = injector.getInstance(Validator.class);
		
		CheckInRepository checkInrepo = injector.getInstance(CheckInRepository.class);
		RequestRepository requestRepo = injector.getInstance(RequestRepository.class);
		businessCtrl = new LocationController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr,feedbackRepo , configProvider);
		
		ddd= injector.getInstance(DummyDataDumper.class);
		
		ddd.generateDummyBusinesses();
		// Do a checkin ...
		checkInData = new CheckInDTO();
		SpotDTO spotDto = checkinCtrl.getSpotInformation("serg2011");
		checkInData.setNickname("PlaceOrderTest");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setSpotId("serg2011");
		checkInData.setUserId(checkinCtrl.createCheckIn( checkInData).getUserId() );
		checkInData.setBusinessId(spotDto.getBusinessId());
		checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		business = rr.getByKey(checkIn.getBusiness());
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testSaveCustomerRequest() {
		RequestDTO requestData = new RequestDTO();
		
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		RequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);
		
		assertThat(result.getCheckInId(), is(checkIn.getId()));
		assertThat(result.getSpotId(), is(checkIn.getSpot().getId()));
		assertThat(result.getId(), notNullValue());
		assertThat(result.getType(), is(requestData.getType()));
		
		List<SpotStatusDTO> spots = businessCtrl.getSpotStatusData(business);
		
		for (SpotStatusDTO spotStatusDTO : spots) {
			if (spotStatusDTO.getId() == checkIn.getSpot().getId()) {
				assertThat(spotStatusDTO.getStatus(), is("CALL_WAITER"));
			}
		}
	}
	
	@Test(expected= NullPointerException.class)
	public void testSaveCustomerRequestNullCheckIn() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		businessCtrl.saveCustomerRequest(null, requestData);
	}
	
	@Test(expected= NullPointerException.class)
	public void testSaveCustomerRequestNullData() {
		businessCtrl.saveCustomerRequest(checkIn, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSaveCustomerRequestInvalidType() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("NOTCALL_WAITER");
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullType() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInId() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setId(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInSpot() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setSpot(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInBusiness() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setBusiness(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected= NullPointerException.class)
	public void testDeleteRequestNullBusiness() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		RequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);

		businessCtrl.deleteCustomerRequest(null, result.getId());

	}
	
	@Test(expected= NullPointerException.class)
	public void testDeleteRequestNullBusinessId() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		RequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(null);
		businessCtrl.deleteCustomerRequest(business, result.getId());
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testDeleteRequestZeroId() {
		businessCtrl.deleteCustomerRequest(business, 0);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testDeleteRequestUnknownId() {
		businessCtrl.deleteCustomerRequest(business, 1234l);
	}

	@Test
	public void testDeleteRequest() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		RequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);

		businessCtrl.deleteCustomerRequest(business, result.getId());
		
		List<SpotStatusDTO> spots = businessCtrl.getSpotStatusData(business);
		
		for (SpotStatusDTO spotStatusDTO : spots) {
			if (spotStatusDTO.getId() == checkIn.getSpot().getId()) {
				assertThat(spotStatusDTO.getStatus(), nullValue());
			}
		}
	}
	
	@Test
	public void testGetRequestDataForCheckIn() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<RequestDTO> result = businessCtrl.getCustomerRequestData(business, checkIn.getId(), null);
		assertThat(result, hasSize(1));
		for (RequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataForSpot() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<RequestDTO> result = businessCtrl.getCustomerRequestData(business, null, checkIn.getSpot().getId());
		assertThat(result, hasSize(1));
		for (RequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataForSpotAndCheckIn() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<RequestDTO> result = businessCtrl.getCustomerRequestData(business, checkIn.getId(), checkIn.getSpot().getId());
		assertThat(result, hasSize(1));
		for (RequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataAll() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<RequestDTO> result = businessCtrl.getCustomerRequestData(business, null, null);
		
		assertThat(result, hasSize(1));
		for (RequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void testGetRequestNullBusiness() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		businessCtrl.getCustomerRequestData(null, null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testGetRequestNullBusinessId() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(null);
		businessCtrl.getCustomerRequestData(business, null, null);
	}
	
	@Test
	public void testGetSpotStatus() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		
		List<SpotStatusDTO> spots = businessCtrl.getSpotStatusData(business);
		assertThat(spots, hasSize(1));
		
		for (SpotStatusDTO spotStatusDTO : spots) {
			assertThat(spotStatusDTO.getStatus(), is("CALL_WAITER"));
		}
	}
	
	@Test(expected= NullPointerException.class)
	public void testGetSpotStatusNullBusiness() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		
		businessCtrl.getSpotStatusData(null);

	}
	
	@Test(expected= NullPointerException.class)
	public void testGetSpotStatusNullBusinessId() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(null);
		businessCtrl.getSpotStatusData(business);
		
	}
	
	@Test
	public void testGetSpotStatusUnknownBusiness() {
		RequestDTO requestData = new RequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(12345l);
		Collection<SpotStatusDTO> result = businessCtrl.getSpotStatusData(business);
		
		assertThat(result.isEmpty(), is(true));
	}
	
	@Test
	public void testUpdateBusiness() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(new PaymentMethod("Visa"));
		paymentMethods.add(new PaymentMethod("EC"));
		paymentMethods.add(new PaymentMethod("Bar"));
		businessData.setPaymentMethods(paymentMethods );
		
		@SuppressWarnings("unchecked")
		Key<Location> businessKey= mock(Key.class);
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		
		Key<Location> key = businessCtrl.updateBusiness(business, businessData );
		
		verify(rr).saveOrUpdate(business);
		assertThat(key, is(businessKey));
		assertThat(business.getAddress(), is(businessData.getAddress()));
		assertThat(business.getCity(), is(businessData.getCity()));
		assertThat(business.getDescription(), is(businessData.getDescription()));
		assertThat(business.getName(), is(businessData.getName()));
		assertThat(business.getPhone(), is(businessData.getPhone()));
		assertThat(business.getPostcode(), is(businessData.getPostcode()));
		assertThat(business.getSlogan(), is(businessData.getSlogan()));
		assertThat(business.getPaymentMethods(), is(paymentMethods));
	}
	
	@Test
	public void testUpdateBusinessNoChanges() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		businessCtrl.updateBusiness(business, businessData );
		business.setDirty(false);
		businessCtrl.updateBusiness(business, businessData );
		verify(rr, times(1)).saveOrUpdate(business);
	}

	/**
	 * @return New instance of {@link LocationController}
	 */
	private LocationController createController() {
		return new LocationController(requestRepo, checkInrepo, br, rr,
				eventBus, accountRepo, imageController, areaRepo, validator,
				mr, feedbackRepo, configProvider);
	}
	
	@Test
	public void testUpdateBusinessSingleProperty() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		businessCtrl.updateBusiness(business, businessData );
		businessData.setName("AnotherTest");
		businessCtrl.updateBusiness(business, businessData );
		verify(rr, times(2)).saveOrUpdate(business);
	}
	
	@Test
	public void testUpdateBusinessPostcodeViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		businessData.setPostcode("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("postcode");
		businessCtrl.updateBusiness(business, businessData );
	}
	
	@Test
	public void testUpdateBusinessCityViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		businessData.setCity("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("city");
		businessCtrl.updateBusiness(business, businessData );
	}
	
	@Test
	public void testUpdateBusinessAddressViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		businessCtrl = createController();
		
		LocationProfileDTO businessData = getTestProfileData();
		businessData.setAddress("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("address");
		businessCtrl.updateBusiness(business, businessData );
	}

	/**
	 * @return Test data to user for update or create business
	 */
	private LocationProfileDTO getTestProfileData() {
		LocationProfileDTO businessData = new LocationProfileDTO();
		businessData.setAddress("test");
		businessData.setCurrency("EUR");
		businessData.setCity("test");
		businessData.setDescription("testesttest");
		businessData.setName("testname");
		businessData.setPhone("test phone");
		businessData.setPostcode("11111");
		businessData.setSlogan("testslogan is great!");
		return businessData;
	}
	
	@Test
	public void testNewBussinessForAccount() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = new Location();
		businessCtrl = createController();
		Configuration config = mock(Configuration.class);
		when(configProvider.get()).thenReturn(config );
		@SuppressWarnings("unchecked")
		Key<FeedbackForm> defaultFeedbackFormKey = mock(Key.class);
		when(config.getDefaultFeedbackForm()).thenReturn(defaultFeedbackFormKey);
		
		FeedbackForm defaultFeedbackForm = mock(FeedbackForm.class);
		when(feedbackRepo.getByKey(defaultFeedbackFormKey)).thenReturn(defaultFeedbackForm );
		@SuppressWarnings("unchecked")
		Key<FeedbackForm> newFormKey = mock(Key.class);
		when(feedbackRepo.saveOrUpdate(defaultFeedbackForm)).thenReturn(newFormKey );
		
		// Mock arguments and stub method calls.
		@SuppressWarnings("unchecked")
		Key<Location> businessKey = mock(Key.class);
		when(rr.getKey(business)).thenReturn(businessKey);
		// Return the mocked class, when the controller retrieves the new instance.
		when(rr.newEntity()).thenReturn(business);
		Account account = mock(Account.class);
		// Create the list here to check the contents after the test.
		List<Key<Location>> businessesList = new ArrayList<Key<Location>>();
		when(account.getBusinesses()).thenReturn(businessesList);
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		LocationProfileDTO testProfileData = getTestProfileData();
		
		// Run the method.
		businessCtrl.createBusinessForAccount(account, testProfileData);
		
		// Verify that save gets called for the entity and for the account.
		verify(accountRepo).saveOrUpdate(account);
		// check that we have at least "bar" payment method
		assertThat(business.getPaymentMethods().get(0).getName(), is("Bar"));
		// The key for the new business should be added to the account.
		assertThat(businessesList, hasItem(businessKey));
		
		// Default FeedbackForm creation verifications.
		verify(defaultFeedbackForm).setId(null);
		assertThat(business.getFeedbackForm(), is(newFormKey));		
	}
	
	@Test
	public void testNewBussinessForAccountNoBusinesses() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = new Location();
		@SuppressWarnings("unchecked")
		Key<Location> businessKey = mock(Key.class);
		when(rr.getKey(business)).thenReturn(businessKey);
		when(rr.newEntity()).thenReturn(business);
		businessCtrl = createController();

		Configuration config = mock(Configuration.class);
		when(configProvider.get()).thenReturn(config );
		
		Account account = mock(Account.class);
		List<Key<Location>> businessesList = new ArrayList<Key<Location>>();
		// First we return null, to test the case of no businesses added.
		when(account.getBusinesses()).thenReturn(null, businessesList );
		
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		// Get test data object.
		LocationProfileDTO testProfileData = getTestProfileData();
		// Run the method.
		businessCtrl.createBusinessForAccount(account, testProfileData);
		
		verify(accountRepo).saveOrUpdate(account);
		// Verify that the new list was set at the account.
		verify(account).setBusinesses(anyList());
		assertThat(businessesList, hasItem(businessKey));
	}
	
	@Test
	public void testUpdateBusinessImage() throws Exception {
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Location.class);
				
		businessCtrl = createController();
		// Mocks for method arguments.
		Account account = mock(Account.class);
		
		// Test data objects.
		ImageDTO updatedImage = new ImageDTO();
		updatedImage.setId("test");
		
		@SuppressWarnings("unchecked")
		List<ImageDTO> images = mock(List.class);
		// Mocks and stubs for dependencies.
		when(imageController.updateImages(account, business.getImages(), updatedImage)).thenReturn(new UpdateImagesResult(images , true, updatedImage));
		// Run the method.
		
		businessCtrl.updateBusinessImage(account, business, updatedImage);
		
		// Verify that we call the save for the entity and the setter for the image list.
		verify(business).setImages(images);
		verify(rr).saveOrUpdate(business);
	}
	
	@Test
	public void testUpdateBusinessImageNoChanges() throws Exception {
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Location.class);
				
		businessCtrl = createController();
		// Mocks for method arguments.
		Account account = mock(Account.class);
		
		// Test data objects.
		ImageDTO updatedImage = new ImageDTO();
		updatedImage.setId("test");
		
		@SuppressWarnings("unchecked")
		List<ImageDTO> images = mock(List.class);
		// Mocks and stubs for dependencies.
		when(imageController.updateImages(account, business.getImages(), updatedImage)).thenReturn(new UpdateImagesResult(images , false, updatedImage));
		// Run the method.
		businessCtrl.updateBusinessImage(account, business, updatedImage);
		
		// Verify that we don't save the entity and don't set the updated image list.
		verify(business,never()).setImages(images);
		verify(rr, never()).saveOrUpdate(business);
	}
	
	@Test
	public void testRemoveBusinessImageNoChanges() throws Exception {
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Location.class);
				
		businessCtrl = createController();
		List<ImageDTO> images = mock(List.class);
		
		String imageId = "test";
		when(imageController.removeImage(imageId, business.getImages())).thenReturn(new UpdateImagesResult(images, false, null));
		businessCtrl.removeBusinessImage(business, imageId);
		
		verify(rr, never()).saveOrUpdate(business);
	}
	
	@Test
	public void testRemoveBusinessImage() throws Exception {
		rr = mock(LocationRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Location.class);
				
		businessCtrl = createController();
		List<ImageDTO> images = mock(List.class);
		
		String imageId = "test";
		when(imageController.removeImage(imageId, business.getImages())).thenReturn(new UpdateImagesResult(images, true, null));
		boolean result = businessCtrl.removeBusinessImage(business, imageId);
		
		assertThat(result, is(true));
		
		verify(business).setImages(images);
		verify(rr).saveOrUpdate(business);
	}
	
	@Test
	public void testUpdateAreaMenus() throws Exception {
		mr = mock(MenuRepository.class);
		businessCtrl = createController();
		
		AreaDTO areaData = getTestAreaData();
		Area area = new Area();
		@SuppressWarnings("unchecked")
		Key<Location> businessKey = mock(Key.class);
		area.setBusiness(businessKey );
		// test update of description
		area.setDescription("Another description.");
		area.setName(areaData.getName());
		area.setActive(areaData.isActive());
		area.setDirty(false);
		
		List<Long> menuIds = new ArrayList<Long>();
		menuIds.add(1l);
		menuIds.add(2l);
		areaData.setMenuIds(menuIds );
		
		@SuppressWarnings("unchecked")
		Key<net.eatsense.domain.Menu> menuKey1 = mock(Key.class);
		@SuppressWarnings("unchecked")
		Key<net.eatsense.domain.Menu> menuKey2 = mock(Key.class);
		when(mr.getKey(businessKey, 1l)).thenReturn(menuKey1 );
		when(mr.getKey(businessKey, 2l)).thenReturn(menuKey2 );
		
		businessCtrl.updateArea(area , areaData);
		
		verify(areaRepo).saveOrUpdate(area);
		assertThat(area.getMenus(), hasItems(menuKey1, menuKey2));	
	}
	
	@Test
	public void testUpdateAreaDescription() throws Exception {
		AreaDTO areaData = getTestAreaData();
		Area area = new Area();
		// test update of description
		area.setDescription("Another description.");
		area.setName(areaData.getName());
		area.setActive(areaData.isActive());
		area.setDirty(false);
		
		
		businessCtrl.updateArea(area , areaData);
		
		verify(areaRepo).saveOrUpdate(area);
		assertThat(area.getDescription(), is(areaData.getDescription()));	
	}
	
	@Test
	public void testUpdateAreaName() throws Exception {
		AreaDTO areaData = getTestAreaData();
		Area area = new Area();
		area.setDescription(areaData.getDescription());
		// test update of name
		area.setName("Old name");
		area.setActive(areaData.isActive());
		area.setDirty(false);
		
		
		businessCtrl.updateArea(area , areaData);
		
		verify(areaRepo).saveOrUpdate(area);
		assertThat(area.getName(), is(areaData.getName()));	
	}
	
	@Test
	public void testUpdateAreaActive() throws Exception {
		AreaDTO areaData = getTestAreaData();
		Area area = new Area();
		area.setDescription(areaData.getDescription());
		area.setName(areaData.getName());
		area.setDirty(false);
		
		// Update active to false.
		areaData.setActive(false);
		
		businessCtrl.updateArea(area , areaData);
		
		verify(areaRepo).saveOrUpdate(area);
		assertThat(area.isActive(), is(false));
		
	}
	
	@Test
	public void testCreateArea() throws Exception {
		
		@SuppressWarnings("unchecked")
		Key<Location> businessKey= mock(Key.class);
		Area area = new Area();
		when(areaRepo.newEntity()).thenReturn(area);
		
		businessCtrl.createArea(businessKey, getTestAreaData());
		verify(areaRepo).saveOrUpdate(area);
		assertThat(area.getBusiness(), is(businessKey));
	}
	
	private AreaDTO getTestAreaData() {
		AreaDTO areaDto = new AreaDTO();
		areaDto.setName("Test area");
		areaDto.setDescription("Test description.");
		areaDto.setActive(true);
		return areaDto;
	}
}
