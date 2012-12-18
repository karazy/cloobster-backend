package net.eatsense.controller.bill;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.controller.BillController;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.exceptions.BillFailureException;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Contains setup and all test cases for the updateBill method.
 * 
 * @author Nils Weiher
 *
 */
public class UpdateBillTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private OrderController orderCtrl;
	    private CheckInController checkinCtrl;
	    private LocationRepository rr;
	    private ProductRepository pr;
	    private DummyDataDumper ddd;

		private Transformer transform;

		private BillController billCtrl;

		private BillRepository br;

		private CheckIn checkIn;

		private Location business;

		private SpotDTO spotDto;

		private Bill newBill;

		private BillDTO billData;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		billCtrl = injector.getInstance(BillController.class);
		rr = injector.getInstance(LocationRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		br = injector.getInstance(BillRepository.class);
		
		transform = injector.getInstance(Transformer.class);
		ddd= injector.getInstance(DummyDataDumper.class);
		
		ddd.generateDummyBusinesses();
		CheckInDTO checkInData = new CheckInDTO();
		
		spotDto = checkinCtrl.getSpotInformation("serg2011");
		checkInData.setNickname("PlaceOrderTest");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setSpotId("serg2011");
		checkInData = checkinCtrl.createCheckIn( checkInData);
		checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		checkInData.setBusinessId(spotDto.getBusinessId()); 
		business = rr.getByKey(checkIn.getBusiness());
		
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		ProductDTO fritesDto = transform.productToDto(frites);
		
		orderDto.setProductId(frites.getId());
		orderDto.setChoices(fritesDto.getChoices());
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		placedOrderDto.setStatus(OrderStatus.PLACED);
		placedOrderDto = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		placedOrderDto.setStatus(OrderStatus.RECEIVED);
		placedOrderDto = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		for (ChoiceDTO choice : burgerDto.getChoices()) {
			if( choice.getText().equals("Extras") )
				for (ProductOption option : choice.getOptions()) {
					if(option.getName().equals("Ei"))
						option.setSelected(true);
					if(option.getName().equals("Salatgurken"))
						option.setSelected(true);
				}
		} 
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProductId(burger.getId());
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		
		placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		placedOrder = orderCtrl.getOrder(business, orderId);
		
		for (ChoiceDTO orderChoice : placedOrderDto.getChoices()) {
			for (ProductOption option : orderChoice.getOptions()) {
				if(option.getName() == selected.getName() )
					assertThat(option.getSelected(), equalTo(true));
			}
		}
		
		// Set order to placed and confirm in restaurant.
		placedOrderDto.setStatus(OrderStatus.PLACED);
		placedOrderDto = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		placedOrderDto.setStatus(OrderStatus.RECEIVED);
		placedOrderDto = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		
		// createBill
		billData = new BillDTO();
		// select any payment method
		PaymentMethod paymentMethod = spotDto.getPayments().iterator().next();
		billData.setPaymentMethod(paymentMethod);
		
		billData = billCtrl.createBill(business, checkIn, billData);
		newBill = billCtrl.getBill(business, billData.getId());
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test(expected = NullPointerException.class)
	public void testUpdateBillNullBusiness() {
		billCtrl.updateBill(null, new Bill(), new BillDTO());
	}
	
	@Test(expected = NullPointerException.class)
	public void testUpdateBillNullBill() {
		billCtrl.updateBill(business, null, new BillDTO());
	}
	
	@Test(expected = NullPointerException.class)
	public void testUpdateBillNullBillData() {
		billCtrl.updateBill(business, new Bill(), null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateBillInvalidBillData() {
		billCtrl.updateBill(business, newBill, new BillDTO());
	}
	
	@Test(expected = NullPointerException.class)
	public void testUpdateBillInvalidBusiness() {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		billCtrl.updateBill(new Location(), newBill, billData);
	}
	
	@Test(expected = BillFailureException.class)
	public void testUpdateBillInvalidBusinessId() {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		Location unknownBusiness = new Location();
		unknownBusiness.setId(666L);
		billCtrl.updateBill(unknownBusiness, newBill, billData);
	}
	
	@Test(expected = NullPointerException.class)
	public void testUpdateBillNullBillId() {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill invalidBill = new Bill();
		invalidBill.setCheckIn(checkIn.getKey());
		
		billCtrl.updateBill(business, invalidBill, billData);
	}
	
	@Test
	public void testUpdateBillInvalidBillId() {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill invalidBill = new Bill();
		invalidBill.setCheckIn(checkIn.getKey());
		invalidBill.setId(666l);
		
		billCtrl.updateBill(business, invalidBill, billData);
	}

	
	@Test
	public void testUpdateBill() {
		//#1 updateBill
		billData.setCleared(true);
		billCtrl.updateBill(business, newBill, billData);
		
		Collection<Bill> bills = br.getAll();
		
		assertThat( bills.size(), is(1));
		 
		for (Bill bill : bills) {
			assertThat(bill.getId(), is(billData.getId()));
			assertThat(bill.getCreationTime(), notNullValue());
			assertThat(bill.getPaymentMethod().getName(), is(billData.getPaymentMethod().getName()));
			assertThat(bill.getTotal(), is(2150l));
			assertThat(bill.isCleared(), is(true));
		}
		// Check orders again to see if they are linked ...
		Iterable<Order> orders = orderCtrl.getOrdersByCheckInOrStatus(business, checkIn.getKey(), null);
		
		for (Order order : orders) {
			assertThat(order.getBill().getId(), is(billData.getId()));
		}
	}
	
}
