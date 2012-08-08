package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.BusinessProfileDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;
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
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class BusinessControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private Injector injector;
	private CheckInController checkinCtrl;
	private BusinessRepository rr;
	private MenuRepository mr;
	private ProductRepository pr;
	private ChoiceRepository cr;
	private OrderRepository or;
	private DummyDataDumper ddd;

	private SpotRepository br;

	private Transformer transform;

	private OrderChoiceRepository ocr;

	private CheckInDTO checkInData;

	private BusinessController businessCtrl;

	private CheckIn checkIn;

	private Business business;

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

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		
		
		checkinCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(BusinessRepository.class);
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
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
				
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
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		CustomerRequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);
		
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
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		businessCtrl.saveCustomerRequest(null, requestData);
	}
	
	@Test(expected= NullPointerException.class)
	public void testSaveCustomerRequestNullData() {
		businessCtrl.saveCustomerRequest(checkIn, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSaveCustomerRequestInvalidType() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("NOTCALL_WAITER");
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullType() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInId() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setId(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInSpot() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setSpot(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSaveCustomerRequestNullCheckInBusiness() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		checkIn.setBusiness(null);
		businessCtrl.saveCustomerRequest(checkIn, requestData);
	}
	
	@Test(expected= NullPointerException.class)
	public void testDeleteRequestNullBusiness() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		CustomerRequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);

		businessCtrl.deleteCustomerRequest(null, result.getId());

	}
	
	@Test(expected= NullPointerException.class)
	public void testDeleteRequestNullBusinessId() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		CustomerRequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);
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
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		CustomerRequestDTO result = businessCtrl.saveCustomerRequest(checkIn, requestData);

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
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<CustomerRequestDTO> result = businessCtrl.getCustomerRequestData(business, checkIn.getId(), null);
		assertThat(result, hasSize(1));
		for (CustomerRequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataForSpot() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<CustomerRequestDTO> result = businessCtrl.getCustomerRequestData(business, null, checkIn.getSpot().getId());
		assertThat(result, hasSize(1));
		for (CustomerRequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataForSpotAndCheckIn() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<CustomerRequestDTO> result = businessCtrl.getCustomerRequestData(business, checkIn.getId(), checkIn.getSpot().getId());
		assertThat(result, hasSize(1));
		for (CustomerRequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test
	public void testGetRequestDataAll() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);

		List<CustomerRequestDTO> result = businessCtrl.getCustomerRequestData(business, null, null);
		
		assertThat(result, hasSize(1));
		for (CustomerRequestDTO customerRequestDTO : result) {
			assertThat(customerRequestDTO.getId(), is(requestData.getId()));
			assertThat(customerRequestDTO.getSpotId(), is(requestData.getSpotId()));
			assertThat(customerRequestDTO.getType(), is(requestData.getType()));
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void testGetRequestNullBusiness() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		businessCtrl.getCustomerRequestData(null, null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testGetRequestNullBusinessId() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(null);
		businessCtrl.getCustomerRequestData(business, null, null);
	}
	
	@Test
	public void testGetSpotStatus() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
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
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		
		businessCtrl.getSpotStatusData(null);

	}
	
	@Test(expected= NullPointerException.class)
	public void testGetSpotStatusNullBusinessId() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn, requestData);
		business.setId(null);
		businessCtrl.getSpotStatusData(business);
		
	}
	
	@Test
	public void testGetSpotStatusUnknownBusiness() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
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
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(new PaymentMethod("Visa"));
		paymentMethods.add(new PaymentMethod("EC"));
		paymentMethods.add(new PaymentMethod("Bar"));
		businessData.setPaymentMethods(paymentMethods );
		
		@SuppressWarnings("unchecked")
		Key<Business> businessKey= mock(Key.class);
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		
		Key<Business> key = businessCtrl.updateBusiness(business, businessData );
		
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
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		businessCtrl.updateBusiness(business, businessData );
		business.setDirty(false);
		businessCtrl.updateBusiness(business, businessData );
		verify(rr, times(1)).saveOrUpdate(business);
	}
	
	@Test
	public void testUpdateBusinessSingleProperty() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		businessCtrl.updateBusiness(business, businessData );
		businessData.setName("AnotherTest");
		businessCtrl.updateBusiness(business, businessData );
		verify(rr, times(2)).saveOrUpdate(business);
	}
	
	@Test
	public void testUpdateBusinessPostcodeViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		businessData.setPostcode("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("postcode");
		businessCtrl.updateBusiness(business, businessData );
	}
	
	@Test
	public void testUpdateBusinessCityViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		businessData.setCity("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("city");
		businessCtrl.updateBusiness(business, businessData );
	}
	
	@Test
	public void testUpdateBusinessAddressViolation() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(BusinessRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		
		BusinessProfileDTO businessData = getTestProfileData();
		businessData.setAddress("");
		thrown.expect(ValidationException.class);
		thrown.expectMessage("address");
		businessCtrl.updateBusiness(business, businessData );
	}

	/**
	 * @return Test data to user for update or create business
	 */
	private BusinessProfileDTO getTestProfileData() {
		BusinessProfileDTO businessData = new BusinessProfileDTO();
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
		rr = mock(BusinessRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = new Business();
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		// Mock arguments and stub method calls.
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock(Key.class);
		when(rr.getKey(business)).thenReturn(businessKey);
		// Return the mocked class, when the controller retrieves the new instance.
		when(rr.newEntity()).thenReturn(business);
		Account account = mock(Account.class);
		// Create the list here to check the contents after the test.
		List<Key<Business>> businessesList = new ArrayList<Key<Business>>();
		when(account.getBusinesses()).thenReturn(businessesList );
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		BusinessProfileDTO testProfileData = getTestProfileData();
		
		// Run the method.
		businessCtrl.createBusinessForAccount(account, testProfileData);
		
		// Verify that save gets called for the entity and for the account.
		verify(accountRepo).saveOrUpdate(account);
		// check that we have at least "bar" payment method
		assertThat(business.getPaymentMethods().get(0).getName(), is("Bar"));
		// The key for the new business should be added to the account.
		assertThat(businessesList, hasItem(businessKey));
	}
	
	@Test
	public void testNewBussinessForAccountNoBusinesses() throws Exception {
		//TODO after refactoring of whole test suite remove this initializiation
		rr = mock(BusinessRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = new Business();
		@SuppressWarnings("unchecked")
		Key<Business> businessKey = mock(Key.class);
		when(rr.getKey(business)).thenReturn(businessKey);
		when(rr.newEntity()).thenReturn(business);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
		Account account = mock(Account.class);
		List<Key<Business>> businessesList = new ArrayList<Key<Business>>();
		// First we return null, to test the case of no businesses added.
		when(account.getBusinesses()).thenReturn(null, businessesList );
		
		when(rr.saveOrUpdate(business)).thenReturn(businessKey);
		// Get test data object.
		BusinessProfileDTO testProfileData = getTestProfileData();
		// Run the method.
		businessCtrl.createBusinessForAccount(account, testProfileData);
		
		verify(accountRepo).saveOrUpdate(account);
		// Verify that the new list was set at the account.
		verify(account).setBusinesses(anyList());
		assertThat(businessesList, hasItem(businessKey));
	}
	
	@Test
	public void testUpdateBusinessImage() throws Exception {
		rr = mock(BusinessRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Business.class);
				
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
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
		rr = mock(BusinessRepository.class);
		accountRepo = mock(AccountRepository.class);
		business = mock(Business.class);
				
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController, areaRepo, validator, mr );
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
}
