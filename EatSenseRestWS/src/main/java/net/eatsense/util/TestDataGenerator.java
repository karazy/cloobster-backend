package net.eatsense.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.eatsense.auth.Role;
import net.eatsense.controller.BillController;
import net.eatsense.controller.ImportController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Company;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.exceptions.BillFailureException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.OfyService;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.LocationImportDTO;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class TestDataGenerator {
	private static final String FULL_LOCATION_IMPORT_FILE = "WEB-INF/classes/import_Cloobster_Club_test.json";
	private static final String BASIC_LOCATION_IMPORT_FILE = "WEB-INF/classes/Basicmode_Test_Location.json";

	private static final String TEST_COMPANY_NAME = "Test Karazy GmbH";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ImportController importController;
	private final InfoPageGenerator infoPageGenerator;
	private final Objectify ofy;
	private final ProductRepository productRepo;
	private final OrderRepository orderRepo;
	private final AccountRepository accountRepo;
	private final ObjectMapper mapper;
	private ListMultimap<Long, Product> products;

	private List<Spot> spots;

	private Random random;
	private static final String TEST_LOGIN = "ctest";
	private static final String TEST_USERLOGIN = "cusertest";

	private final BillController billCtrl;
	
	@Inject
	public TestDataGenerator(BillController billCtrl, ImportController importController, InfoPageGenerator infoPageGenerator, OfyService ofyService, ProductRepository productRepo, OrderRepository orderRepo, AccountRepository accountRepo, ObjectMapper mapper) {
		this.billCtrl = billCtrl;
		this.importController = importController;
		this.infoPageGenerator = infoPageGenerator;
		this.productRepo = productRepo;
		this.accountRepo = accountRepo;
		this.mapper = mapper;
		this.ofy = ofyService.ofy();
		this.orderRepo = orderRepo;
	}
	
	
	
	public void createTestData() {
		random = new Random();
		deleteTestData();
		Company testCompany = new Company();
		testCompany.setAddress("An den Krautgärten 15");
		testCompany.setCity("Eschborn");
		testCompany.setCountry("DE");
		testCompany.setName(TEST_COMPANY_NAME);
		testCompany.setPostcode("65760");
		testCompany.setUrl("http://www.karazy.net/");
		Key<Company> companyKey = ofy.put(testCompany);
		Account account = accountRepo.createAndSaveAccount("Cloobster Test Admin", TEST_LOGIN, "cl00bster!", "developer@karazy.net", Role.COMPANYOWNER, null, companyKey, null, null, true, true);
		LocationImportDTO fullLocationImport; 
		try {
			fullLocationImport = mapper.readValue(new File(FULL_LOCATION_IMPORT_FILE), LocationImportDTO.class);
		} catch (Exception e) {
			logger.error("error parsing json import", e);
			throw new ServiceException(e);
		}
		
		Business business1 = createTestCompleteBusiness(fullLocationImport, account);
		
		LocationImportDTO basicLocationImport; 
		try {
			basicLocationImport = mapper.readValue(new File(BASIC_LOCATION_IMPORT_FILE), LocationImportDTO.class);
		} catch (Exception e) {
			logger.error("error parsing json import", e);
			throw new ServiceException(e);
		}
		Business business2 = createBasicBusiness(basicLocationImport, account);
		
		account.setBusinesses(Arrays.asList(business1.getKey(), business2.getKey()));
		accountRepo.saveOrUpdate(account);
		
		createTestUserAccount(business1, 20);
	}
	
	private Business createBasicBusiness(LocationImportDTO locationData,
			Account account) {
		Business business = importController.addBusiness(locationData, account.getCompany() );
		Key<Business> businessKey = business.getKey();
		
		Subscription sub = new Subscription();
		sub.setBasic(true);
		sub.setBusiness(businessKey);
		sub.setFee(0);
		sub.setMaxSpotCount(1);
		sub.setName("Test Basic Subscription");
		sub.setStartDate(new Date());
		sub.setStatus(SubscriptionStatus.APPROVED);
		Key<Subscription> subKey = ofy.put(sub);
		business.setBasic(true);
		business.setActiveSubscription(subKey);
		
		ofy.put(business);
		
		infoPageGenerator.generate(businessKey, 20, new Locale("en"));
		return business;
	}

	private void createTestUserAccount(Business business, int numberOfPastCheckIns) {
		Account account = accountRepo.createAndSaveAccount("Cloobster Test User", TEST_USERLOGIN, "test11", "developer@karazy.net", Role.USER, null, null, null, null, true, true);
		
		for (int i = 0; i < numberOfPastCheckIns; i++) {
			Spot spot = spots.get(random.nextInt(spots.size()));
			CheckIn checkIn = createAndSaveCheckIn(spot, CheckInStatus.COMPLETE, "Test User", account.getKey());
			List<Order> orders = createTestOrders(checkIn, spot, OrderStatus.COMPLETE, random.nextInt(3)+1);
			createTestBill(checkIn, orders, business.getPaymentMethods().get(0), spot, CurrencyUnit.of(business.getCurrency()), true);
		}
	}

	public void deleteTestData() {
		logger.info("deleting all previous data for test account: {}", TEST_LOGIN );
		
		Account companyAccount = accountRepo.getByProperty("login", TEST_LOGIN);
		
		if(companyAccount != null) {
			if(companyAccount.getBusinesses() != null) {
				for(Key<Business> businessKey : companyAccount.getBusinesses()) {
					ofy.async().delete(ofy.query(CheckIn.class).filter("business", businessKey).fetchKeys());
					ofy.async().delete(ofy.query().ancestor(businessKey).fetchKeys());
				}	
			}
			ofy.delete(companyAccount.getCompany(), companyAccount);
		}
		
		Account userAccount = accountRepo.getByProperty("login", TEST_USERLOGIN);
		
		if(userAccount != null) {
			ofy.delete(userAccount);
		}
	}

	private Business createTestCompleteBusiness(LocationImportDTO locationData, Account account) {
		Business business = importController.addBusiness(locationData, account.getCompany() );
		Key<Business> businessKey = business.getKey();
		
		Subscription sub = new Subscription();
		sub.setBasic(false);
		sub.setBusiness(businessKey);
		sub.setFee(0);
		sub.setMaxSpotCount(0);
		sub.setName("Test Subscription");
		sub.setStartDate(new Date());
		sub.setStatus(SubscriptionStatus.APPROVED);
		Key<Subscription> subKey = ofy.put(sub);
		business.setBasic(false);
		business.setActiveSubscription(subKey);
		
		ofy.put(business);
		
		infoPageGenerator.generate(businessKey, 20, new Locale("en"));
		
		products = ArrayListMultimap.create();
		// Sort products by menu
		for(Product product : importController.getImportedProducts()) {
			products.put(product.getMenu().getId(), product);
		}
		
		
		
		spots = new ArrayList<Spot>();
		for(Spot spot : importController.getSpots()) {
			if(!spot.isWelcome())
				spots.add(spot);
		}
		
		
		
		if(spots.size() == 0) {
			// UNPOSSIBLE!!
			logger.error("No Spot found in Test Data! Stopping creation");
		}
		Spot testSpot = spots.get(random.nextInt(spots.size()));
						
		CheckIn checkIn = createAndSaveCheckIn(testSpot, CheckInStatus.ORDER_PLACED, "Test Open Orders", null);
		createTestOrders(checkIn,testSpot, OrderStatus.PLACED, 3);
		
		testSpot = spots.get(random.nextInt(spots.size()));
		createAndSaveCheckIn(testSpot, CheckInStatus.CHECKEDIN, "Test CheckedIn", null);
		
		testSpot = spots.get(random.nextInt(spots.size()));
		checkIn = createAndSaveCheckIn(testSpot, CheckInStatus.PAYMENT_REQUEST, "Test Payment Request", null);
		List<Order> orders = createTestOrders(checkIn, testSpot, OrderStatus.RECEIVED, 2);
		createTestBill(checkIn, orders, business.getPaymentMethods().get(0), testSpot, CurrencyUnit.of(business.getCurrency()), false);
		
		return business;
	}
	
	private Bill createTestBill(CheckIn checkIn, List<Order> orders, PaymentMethod method, Spot spot, CurrencyUnit currencyUnit, boolean cleared) {
		Bill bill = new Bill();
		bill.setPaymentMethod(method);
		bill.setBusiness(checkIn.getBusiness());
		bill.setCheckIn(checkIn.getKey());
		bill.setCreationTime(new Date());
		bill.setCleared(cleared);
		
		Money billTotal = Money.of(currencyUnit, 0);
		
		for (Order order : orders) {
			if(order.getStatus() == OrderStatus.COMPLETE) {
				billTotal = billTotal.plus(billCtrl.calculateTotalPrice(order, currencyUnit));
			}
		}
		
		bill.setTotal(billTotal.getAmountMinorLong());
		
		ofy.put(bill);
		
		if(!cleared) {
			Request request = new Request(checkIn, spot, bill);
			request.setObjectText(bill.getPaymentMethod().getName());
			request.setStatus(CheckInStatus.PAYMENT_REQUEST.toString());
			ofy.put(request);
		}
		
		return bill;
	}

	/**
	 * @param checkIn
	 * @param status
	 * @param number
	 */
	private List<Order> createTestOrders(CheckIn checkIn, Spot spot, OrderStatus status, int number) {
		if(number == 0) 
			return Collections.emptyList();
		
		Random random = new Random();
		ArrayList<Order> orders = new ArrayList<Order>();
		ArrayList<Request> requests = new ArrayList<Request>();
		Area area = ofy.get(checkIn.getArea());
		
		List<Product> availableProducts = new ArrayList<Product>();
		
		if(area.getMenus() == null) {
			logger.error("No menus assigned to area, skipping creation of orders");
			return Collections.emptyList();
		}
		
		for(Key<Menu> menuKey : area.getMenus()) {
			availableProducts.addAll(products.get(menuKey.getId()));
		}
		
		for (int i = 0; i < number; i++) {
			Product randomProduct = availableProducts.get(random.nextInt(availableProducts.size()));
			logger.info("Create Order for : {}", randomProduct.getKey());
			Order newOrder = new Order();
			newOrder.setAmount(1);
			newOrder.setBusiness(checkIn.getBusiness());
			Key<CheckIn> checkInKey = checkIn.getKey();
			newOrder.setCheckIn(checkInKey);
			newOrder.setComment("");
			newOrder.setOrderTime(new Date());
			newOrder.setProduct(randomProduct.getKey());
			newOrder.setProductLongDesc(randomProduct.getLongDesc());
			newOrder.setProductName(randomProduct.getName());
			newOrder.setProductPrice(randomProduct.getPrice());
			newOrder.setProductShortDesc(randomProduct.getShortDesc());
			newOrder.setStatus(status);
			orders.add(newOrder);
			
			if(status == OrderStatus.PLACED) {
				Request request = new Request(checkIn, spot, newOrder);
				request.setObjectText(newOrder.getProductName());
				request.setStatus(CheckInStatus.ORDER_PLACED.toString());
				
				requests.add(request);
			}
		}
		ofy.put(orders);
		ofy.put(requests);
		
		return orders;
	}

	private CheckIn createAndSaveCheckIn(Spot spot, CheckInStatus status, String nickname, Key<Account> accountKey) {
		CheckIn checkIn = new CheckIn();
		checkIn.setArea(spot.getArea());
		checkIn.setBusiness(spot.getBusiness());
		checkIn.setCheckInTime(new Date());
		checkIn.setNickname(nickname);
		checkIn.setSpot(spot.getKey());
		checkIn.setStatus(status);
		if(status == CheckInStatus.COMPLETE) 
			checkIn.setArchived(true);
		checkIn.setUserId(IdHelper.generateId());
		checkIn.setAccount(accountKey);
		
		ofy.put(checkIn);
		
		return checkIn;
	}
}
