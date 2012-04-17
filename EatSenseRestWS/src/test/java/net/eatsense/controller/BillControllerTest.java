package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BillControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	    
	    private Injector injector;
	    private OrderController orderCtrl;
	    private CheckInController checkinCtrl;
	    private BusinessRepository rr;
	    private MenuRepository mr;
	    private ProductRepository pr;
	    private ChoiceRepository cr;
	    private OrderRepository or;
	    private DummyDataDumper ddd;

		private SpotRepository sr;

		private Transformer transform;

		private OrderChoiceRepository ocr;

		private BillController billCtrl;

		private BillRepository br;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		billCtrl = injector.getInstance(BillController.class);
		rr = injector.getInstance(BusinessRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		sr = injector.getInstance(SpotRepository.class);
		br = injector.getInstance(BillRepository.class);
		
		or = injector.getInstance(OrderRepository.class);
		ocr = injector.getInstance(OrderChoiceRepository.class);
		
		transform = injector.getInstance(Transformer.class);
		
		
		
		ddd= injector.getInstance(DummyDataDumper.class);
		
		ddd.generateDummyBusinesses();
		
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testCreateBill() {
		// Do a checkin ...
		CheckInDTO checkIn = new CheckInDTO();
		SpotDTO spotDto = checkinCtrl.getSpotInformation("serg2011");
		checkIn.setNickname("PlaceOrderTest");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn.setSpotId("serg2011");
		checkIn.setUserId(checkinCtrl.createCheckIn( checkIn).getUserId() );
		checkIn.setBusinessId(spotDto.getBusinessId());
		
		
		assertThat(checkIn.getUserId(), notNullValue());
		
				
		// Should be checked in
		//assertThat(checkIn.getStatus(), equalTo(CheckInStatus.CHECKEDIN.toString()) );
		
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		orderDto.setProduct(transform.productToDto(frites));
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(checkIn.getBusinessId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkIn.getBusinessId(), orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProduct().getId(), equalTo(frites.getId()));
		
		placedOrder.setStatus(OrderStatus.PLACED);
		placedOrder = orderCtrl.updateOrder(checkIn.getBusinessId(), orderId, placedOrder, checkIn.getUserId());
		placedOrder.setStatus(OrderStatus.RECEIVED);
		placedOrder = orderCtrl.updateOrderForBusiness(checkIn.getBusinessId(), orderId, placedOrder);
		
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
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(checkIn.getBusinessId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrderAsDTO(checkIn.getBusinessId(), orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProduct().getId(), equalTo(burger.getId()));
		assertThat(placedOrder.getProduct().getChoices(), notNullValue());
		for (ChoiceDTO orderChoice : placedOrder.getProduct().getChoices()) {
			for (ProductOption option : orderChoice.getOptions()) {
				if(option.getName() == selected.getName() )
					assertThat(option.getSelected(), equalTo(true));
			}
		}
		// Set order to placed and confirm in restaurant.
		placedOrder.setStatus(OrderStatus.PLACED);
		placedOrder = orderCtrl.updateOrder(checkIn.getBusinessId(), orderId, placedOrder, checkIn.getUserId());
		placedOrder.setStatus(OrderStatus.RECEIVED);
		placedOrder = orderCtrl.updateOrderForBusiness(checkIn.getBusinessId(), orderId, placedOrder);
		
		
		//#3 Check calculateTotalPrice
		
		List<Order> orders = orderCtrl.getOrders(checkIn.getBusinessId(), checkIn.getUserId(), null);
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(2));
		for (Order order : orders) {
			assertThat(order.getStatus(), equalTo(OrderStatus.RECEIVED));
			if(order.getProduct().getId() == frites.getId()) {
				assertThat(billCtrl.calculateTotalPrice(order), is( 1.5f));
			}
			if(order.getProduct().getId() == burger.getId()) {
				assertThat(billCtrl.calculateTotalPrice(order), is( 10f));
			}
		}
		
		//#4 Test createBill
		BillDTO billData = new BillDTO();
		// select any payment method
		PaymentMethod paymentMethod = spotDto.getPayments().iterator().next();
		billData.setPaymentMethod(paymentMethod);
		
		billData = billCtrl.createBill(checkIn.getBusinessId(), checkIn.getUserId(), billData);
		assertThat(billData.getId(), notNullValue());
		billData.setCleared(true);
		
		
		billCtrl.updateBill(checkIn.getBusinessId(), billData.getId(), billData);
		
		Collection<Bill> bills = br.getAll();
		
		assertThat( bills.size(), is(1));
		 
		for (Bill bill : bills) {
			assertThat(bill.getId(), is(billData.getId()));
			assertThat(bill.getCreationTime(), notNullValue());
			assertThat(bill.getPaymentMethod().getName(), is(paymentMethod.getName()));
			assertThat(bill.getTotal(), is(11.5f));
		}
		// Check orders again to see if they are linked ...
		orders = orderCtrl.getOrders(checkIn.getBusinessId(), checkIn.getUserId(), null);
		
		for (Order order : orders) {
			assertThat(order.getBill().getId(), is(billData.getId()));
		}
	}
	
}
