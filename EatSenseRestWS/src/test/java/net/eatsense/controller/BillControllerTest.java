package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
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

		private CheckIn checkIn;

		private Business business;

		private SpotDTO spotDto;

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
		CheckInDTO checkInData = new CheckInDTO();
		spotDto = checkinCtrl.getSpotInformation("serg2011");
		checkInData.setNickname("PlaceOrderTest");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData.setSpotId("serg2011");
		checkInData = checkinCtrl.createCheckIn( checkInData);
		checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		checkInData.setBusinessId(spotDto.getBusinessId());
		 
		business = rr.getByKey(checkIn.getBusiness());
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testCreateBill() {
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		orderDto.setProduct(transform.productToDto(frites));
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		assertThat(placedOrderDto.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrderDto.getOrderTime(), notNullValue());
		assertThat(placedOrderDto.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrderDto.getProduct().getId(), equalTo(frites.getId()));
		
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
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		placedOrder = orderCtrl.getOrder(business, orderId);
		
		assertThat(placedOrderDto.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrderDto.getOrderTime(), notNullValue());
		assertThat(placedOrderDto.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrderDto.getProduct().getId(), equalTo(burger.getId()));
		assertThat(placedOrderDto.getProduct().getChoices(), notNullValue());
		for (ChoiceDTO orderChoice : placedOrderDto.getProduct().getChoices()) {
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
		
		
		//#3 Check calculateTotalPrice
		
		List<Order> orders = orderCtrl.getOrders(business, checkIn, null);
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
		
		billData = billCtrl.createBill(business, checkIn, billData);
		Bill newBill = billCtrl.getBill(business, billData.getId());
		assertThat(billData.getId(), notNullValue());
		billData.setCleared(true);
		
		
		billCtrl.updateBill(business, newBill, billData);
		
		Collection<Bill> bills = br.getAll();
		
		assertThat( bills.size(), is(1));
		 
		for (Bill bill : bills) {
			assertThat(bill.getId(), is(billData.getId()));
			assertThat(bill.getCreationTime(), notNullValue());
			assertThat(bill.getPaymentMethod().getName(), is(paymentMethod.getName()));
			assertThat(bill.getTotal(), is(11.5f));
		}
		// Check orders again to see if they are linked ...
		orders = orderCtrl.getOrders(business, checkIn, null);
		
		for (Order order : orders) {
			assertThat(order.getBill().getId(), is(billData.getId()));
		}
	}
	
}
