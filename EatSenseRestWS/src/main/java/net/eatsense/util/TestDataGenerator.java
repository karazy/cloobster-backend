package net.eatsense.util;

import java.util.Date;
import java.util.Locale;

import net.eatsense.controller.ImportController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Company;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.persistence.OfyService;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.LocationImportDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class TestDataGenerator {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ImportController importController;
	private final InfoPageGenerator infoPageGenerator;
	private final Objectify ofy;
	private final ProductRepository productRepo;
	private final OrderRepository orderRepo;
	
	@Inject
	public TestDataGenerator(ImportController importController, InfoPageGenerator infoPageGenerator, OfyService ofyService, ProductRepository productRepo, OrderRepository orderRepo) {
		this.importController = importController;
		this.infoPageGenerator = infoPageGenerator;
		this.productRepo = productRepo;
		this.ofy = ofyService.ofy();
		this.orderRepo = orderRepo;
	}
	
	private Key<Business> createTestCompleteBusiness(LocationImportDTO locationData, Key<Company> companyKey) {
		Key<Business> businessKey = importController.addBusiness(locationData, companyKey );
		infoPageGenerator.generate(businessKey, 20, null);
		infoPageGenerator.generate(businessKey, 20, new Locale("en"));
		
		Spot testSpot = null;
		for( Spot spot : ofy.query(Spot.class).ancestor(businessKey) ) {
			if(!spot.isWelcome()) {
				testSpot = spot;
			}
		}
		if(testSpot == null) {
			// UNPOSSIBLE!!
			logger.error("No Spot found in Test Data! Stopping creation");
		}
						
		CheckIn checkInKey = createAndSaveCheckIn(testSpot, CheckInStatus.ORDER_PLACED, "Test Open Orders");
		createTestOrders(checkInKey, OrderStatus.PLACED, 3);
		
		createAndSaveCheckIn(testSpot, CheckInStatus.CHECKEDIN, "Test CheckedIn");
		createAndSaveCheckIn(testSpot, CheckInStatus.PAYMENT_REQUEST, "Test Payment Request");
		
		return businessKey;
	}
	
	private void createTestOrders(CheckIn checkInKey, OrderStatus placed, int i) {
				
	}

	private CheckIn createAndSaveCheckIn(Spot spot, CheckInStatus status, String nickname) {
		CheckIn checkIn = new CheckIn();
		checkIn.setArea(spot.getArea());
		checkIn.setBusiness(spot.getBusiness());
		checkIn.setCheckInTime(new Date());
		checkIn.setNickname(nickname);
		checkIn.setSpot(spot.getKey());
		checkIn.setStatus(status);
		checkIn.setUserId(IdHelper.generateId());
		
		return checkIn;
	}
}
