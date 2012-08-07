package net.eatsense.controller.bill;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.controller.BillController;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.persistence.BusinessRepository;
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
 * Contains setup and all test cases for createBill method.
 * 
 * @author Nils Weiher
 *
 */
public class CreateBillTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private OrderController orderCtrl;
	    private CheckInController checkinCtrl;
	    private BusinessRepository rr;
	    private ProductRepository pr;
	    private DummyDataDumper ddd;
		private Transformer transform;
		private BillController billCtrl;

		private CheckIn checkIn;
		private Business business;
		private SpotDTO spotDto;

		private BillDTO billData;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		billCtrl = injector.getInstance(BillController.class);
		rr = injector.getInstance(BusinessRepository.class);
		pr = injector.getInstance(ProductRepository.class);
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
		orderDto.setProductId(frites.getId());
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

		// Set order to placed and confirm in restaurant.
		placedOrderDto.setStatus(OrderStatus.PLACED);
		placedOrderDto = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		placedOrderDto.setStatus(OrderStatus.RECEIVED);
		placedOrderDto = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		
		billData = new BillDTO();
		// select any payment method
		PaymentMethod paymentMethod = spotDto.getPayments().iterator().next();
		billData.setPaymentMethod(paymentMethod);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullBusiness() {
		billCtrl.createBill(null, checkIn, billData);
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullCheckIn() {
		billCtrl.createBill(business, null, billData);
	}
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullCheckInId() {
		checkIn.setId(null);
		billCtrl.createBill(business, checkIn, billData);
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullCheckInSpot() {
		checkIn.setSpot(null);
		billCtrl.createBill(business, checkIn, billData);
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullBillData() {
		billCtrl.createBill(business, checkIn, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullPaymentMethod() {
		billCtrl.createBill(business, checkIn, new BillDTO());
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateBillNullBusinessPaymentMethod() {
		business.setPaymentMethods(null);
		billCtrl.createBill(business, checkIn, billData);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBillEmptyBusinessPaymentMethod() {
		business.setPaymentMethods(new ArrayList<PaymentMethod>());
		billCtrl.createBill(business, checkIn, billData);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBillInvalidPaymentMethod() {
		BillDTO invalidData = new BillDTO();
		invalidData.setPaymentMethod(new PaymentMethod());
		billCtrl.createBill(business, checkIn, invalidData);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateBillInvalidCheckInStatus() {
		checkIn.setStatus(null);
		billCtrl.createBill(business, checkIn, billData);
	}
	
	@Test
	public void testCreateBillId() {
		billData = billCtrl.createBill(business, checkIn, billData);
		assertThat(billData.getId(), notNullValue());
		Bill newBill = billCtrl.getBill(business, billData.getId());
		assertThat(newBill.getId(), is(billData.getId()));
	}
	
	@Test
	public void testCreateBillPaymentMethod() {
		billData = billCtrl.createBill(business, checkIn, billData);
		Bill newBill = billCtrl.getBill(business, billData.getId());
		assertThat(newBill.getPaymentMethod().getName(), is(billData.getPaymentMethod().getName()));		
	}
}
