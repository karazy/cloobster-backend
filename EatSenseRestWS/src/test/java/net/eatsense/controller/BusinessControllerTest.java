package net.eatsense.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CustomerRequestDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.SpotStatusDTO;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

@RunWith(MockitoJUnitRunner.class)
public class BusinessControllerTest {
	
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
		
		ImageController imageController = new ImageController(blobstoreService, imagesService, accountRepo);
		CheckInRepository checkInrepo = injector.getInstance(CheckInRepository.class);
		RequestRepository requestRepo = injector.getInstance(RequestRepository.class);
		businessCtrl = new BusinessController(requestRepo, checkInrepo , br, rr , eventBus, accountRepo, imageController );
		
		
		
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
}
