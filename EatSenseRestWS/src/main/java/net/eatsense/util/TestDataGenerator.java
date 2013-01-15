package net.eatsense.util;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.eatsense.auth.Role;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Company;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.persistence.AccountRepository;
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
	private static final String TEST_COMPANY_NAME = "Test Karazy GmbH";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ImportController importController;
	private final InfoPageGenerator infoPageGenerator;
	private final Objectify ofy;
	private final ProductRepository productRepo;
	private final OrderRepository orderRepo;
	private final AccountRepository accountRepo;

	private List<Product> products;

	private static final String TEST_LOGIN = "cloobster";
	
	@Inject
	public TestDataGenerator(ImportController importController, InfoPageGenerator infoPageGenerator, OfyService ofyService, ProductRepository productRepo, OrderRepository orderRepo, AccountRepository accountRepo) {
		this.importController = importController;
		this.infoPageGenerator = infoPageGenerator;
		this.productRepo = productRepo;
		this.accountRepo = accountRepo;
		this.ofy = ofyService.ofy();
		this.orderRepo = orderRepo;
	}
	
	public void createTestData() {
		deleteTestData();
		Company testCompany = new Company();
		testCompany.setAddress("An den Krautgärten 15");
		testCompany.setCity("Eschborn");
		testCompany.setCountry("DE");
		testCompany.setName(TEST_COMPANY_NAME);
		testCompany.setPostcode("65760");
		testCompany.setUrl("http://www.karazy.net/");
		Key<Company> companyKey = ofy.put(testCompany);
		Account account = accountRepo.createAndSaveAccount("Cloobster Test Admin", "cloobster", "cl00bster!", "developer@karazy.net", Role.COMPANYOWNER, null, companyKey, null, null, true, true);
		
		
	}
	
	private void deleteTestData() {
		logger.info("deleting all previous data for test account: {}", TEST_LOGIN );
		
		Account account = accountRepo.getByProperty("login", TEST_LOGIN);
				
		for(Key<Business> businessKey : account.getBusinesses()) {
			ofy.async().delete(ofy.query(CheckIn.class).filter("business", businessKey).fetchKeys());
			ofy.async().delete(ofy.query().ancestor(businessKey).fetchKeys());
		}
		
		ofy.delete(account.getCompany(), account);
	}

	private Key<Business> createTestCompleteBusiness(LocationImportDTO locationData, Account account) {
		Key<Business> businessKey = importController.addBusiness(locationData, account.getCompany() );
		
		infoPageGenerator.generate(businessKey, 20, null);
		infoPageGenerator.generate(businessKey, 20, new Locale("en"));
		
		products = importController.getImportedProducts();
		
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
		createTestOrders(checkInKey, OrderStatus.RECEIVED, 2);
		
		return businessKey;
	}
	
	/**
	 * @param checkIn
	 * @param status
	 * @param number
	 */
	private List<Order> createTestOrders(CheckIn checkIn, OrderStatus status, int number) {		
		Random random = new Random();
		ArrayList<Order> orders = new ArrayList<Order>();
		
		for (int i = 0; i < number; i++) {
			Product randomProduct = products.get(random.nextInt(products.size()));
			logger.info("Create Order for : {}", randomProduct.getKey());
			Order newOrder = new Order();
			newOrder.setAmount(1);
			newOrder.setBusiness(checkIn.getBusiness());
			newOrder.setCheckIn(checkIn.getKey());
			newOrder.setOrderTime(new Date());
			newOrder.setProduct(randomProduct.getKey());
			newOrder.setProductLongDesc(randomProduct.getLongDesc());
			newOrder.setProductName(randomProduct.getName());
			newOrder.setProductPrice(randomProduct.getPrice());
			newOrder.setProductShortDesc(randomProduct.getShortDesc());
			newOrder.setStatus(status);
			orders.add(newOrder);
		}
		ofy.put(orders);
		
		return orders;
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
