package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.NotFoundException;

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

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		businessCtrl = injector.getInstance(BusinessController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(BusinessRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		or = injector.getInstance(OrderRepository.class);
		ocr = injector.getInstance(OrderChoiceRepository.class);
		transform = injector.getInstance(Transformer.class);
		
		
		
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
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testSaveAndDeleteRequest() {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		CheckIn checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		assertThat(checkIn.getUserId(), is( checkInData.getUserId()));
		
		requestData.setType("CALL_WAITER");
		// Save a call waiter request.
		requestData = businessCtrl.saveCustomerRequest(checkIn.getUserId(), requestData);
		
		// Save a call waiter request for unknown checkin
		try {
			businessCtrl.saveCustomerRequest("unknowncheckinid", requestData);
		} catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
		}
		
		// Save a call waiter request for null checkin
		try {
			businessCtrl.saveCustomerRequest(null, requestData);
		} catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
		}
		
		assertThat(requestData.getId(), notNullValue());
		assertThat(requestData.getCheckInId(), is(checkIn.getId()));
		
		List<SpotStatusDTO> spots = businessCtrl.getSpotStatusData(checkInData.getBusinessId());
		
		for (SpotStatusDTO spotStatusDTO : spots) {
			if (spotStatusDTO.getId() == checkIn.getSpot().getId()) {
				assertThat(spotStatusDTO.getStatus(), is("CALL_WAITER"));
			}
		}
		
		businessCtrl.deleteCustomerRequest(checkInData.getBusinessId(), requestData.getId());
		
		spots = businessCtrl.getSpotStatusData(checkInData.getBusinessId());
		
		for (SpotStatusDTO spotStatusDTO : spots) {
			if (spotStatusDTO.getId() == checkIn.getSpot().getId()) {
				assertThat(spotStatusDTO.getStatus(), nullValue());
			}
		}
		
	}
}
