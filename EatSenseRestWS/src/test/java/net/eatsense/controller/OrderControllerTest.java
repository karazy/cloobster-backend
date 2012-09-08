package net.eatsense.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.exceptions.OrderFailureException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.SpotRepository;
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

public class OrderControllerTest {
	
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

		private SpotRepository br;

		private Transformer transform;

		private OrderChoiceRepository ocr;

		private CheckInDTO checkInData;

		private CheckIn checkIn;

		private Business business;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
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
		checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		business = rr.getByKey(checkIn.getBusiness());
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testPlaceAndUpdateOrder() {
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		ProductDTO productDTO = transform.productToDto(frites);
		orderDto.setChoices(productDTO.getChoices());
		orderDto.setProductId(frites.getId());
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		
		assertThat(placedOrderDto.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrderDto.getOrderTime(), notNullValue());
		assertThat(placedOrderDto.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrderDto.getProductId(), equalTo(frites.getId()));
		
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProductId(burger.getId());
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		assertThat(placedOrderDto.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrderDto.getOrderTime(), notNullValue());
		assertThat(placedOrderDto.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrderDto.getProductId(), equalTo(burger.getId()));
		assertThat(placedOrderDto.getChoices(), notNullValue());
		for (ChoiceDTO orderChoice : placedOrderDto.getChoices()) {
			for (ProductOption option : orderChoice.getOptions()) {
				if(option.getName() == selected.getName() )
					assertThat(option.getSelected(), equalTo(true));
			}
		}
		
		//#3 Check "getOrders"
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(business, checkIn.getKey(), "cart");
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(2));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
		
		//#4.1 Update order to placed
		placedOrderDto.setStatus(OrderStatus.PLACED);
		placedOrderDto = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		assertThat(placedOrderDto.getStatus(), is(OrderStatus.PLACED) );
		
		//#4.2 Try to update order after status was set to PLACED
		
		OrderDTO result;
		try {
			placedOrderDto.setStatus(OrderStatus.CART);
			result = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		} catch (Exception e) {
			assertThat(e, instanceOf(OrderFailureException.class));
		}
	}
	
	@Test
	public void testProductOptions() {
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(false);
		
		
		OrderDTO orderDto = new OrderDTO();
		orderDto.setStatus(OrderStatus.CART);
		orderDto .setId(null);
		orderDto.setAmount(2);
		orderDto.setProductId(burger.getId());
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setComment("I like my burger " + selected.getName());
		
		Long orderId = null;
		try {
			orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		} catch (Exception e) {
			assertThat(e, instanceOf(ValidationException.class));
		}
		assertThat(orderId, nullValue());
		selected = orderDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		// Place an order with correct choices
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		// Test validation of dependent choice
		// Select the menu item next
		boolean menuSelected = false;
		for(ChoiceDTO choice : orderDto.getChoices()) {
			if(choice.getText().equals("Menü")) {
				menuSelected = true;
				choice.getOptions().iterator().next().setSelected(true);
			}
		}
		assertThat(menuSelected, is(true));
	}
	
	@Test
	public void testDeleteOrder() {
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
		assertThat(orderId, notNullValue());
		
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		orderCtrl.deleteOrder(business, placedOrder , checkIn);
		
		Iterable<Order> orders = orderCtrl.getOrdersByCheckInOrStatus(business, checkIn.getKey(), null);
		List<OrderChoice> choices = ocr.getByParent(Order.getKey(Business.getKey(checkInData.getBusinessId()), orderId));
		assertThat(choices.isEmpty(), is(true));
		assertThat(orders.iterator().hasNext(), is(false));
		
		//#2.1 Place a simple order without choices...
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrder(business, orderId);
		
		//#2.2 Update the order to PLACED
		orderDto.setStatus(OrderStatus.PLACED);
		orderDto = orderCtrl.updateOrder(business, placedOrder, orderDto, checkIn);
		
		//#2.3 Try to delete the order, it should fail.
		try {
			orderCtrl.deleteOrder(business, placedOrder , checkIn);
		} catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
		}				
	}
	
	@Test public void testGetOrdersWithStatus() {
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
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(business, orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProductId(), equalTo(frites.getId()));
		
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setProductId(burger.getId());
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrderAsDTO(business, orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProductId(), equalTo(burger.getId()));
		assertThat(placedOrder.getChoices(), notNullValue());
		for (ChoiceDTO orderChoice : placedOrder.getChoices()) {
			for (ProductOption option : orderChoice.getOptions()) {
				if(option.getName() == selected.getName() )
					assertThat(option.getSelected(), equalTo(true));
			}
		}
		//#3 Check "getOrders" with status
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(business, checkIn.getKey(), "CART");
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(2));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
	}
	
	@Test
	public void testUpdateOrderAsBusiness() {
		assertThat(checkInData.getUserId(), notNullValue());
		
		// Should be checked in
		//assertThat(checkIn.getStatus(), equalTo(CheckInStatus.CHECKEDIN.toString()) );
		
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		ProductDTO fritesDto = transform.productToDto(frites);
		orderDto.setChoices(fritesDto.getChoices());
		orderDto.setProductId(frites.getId());
		orderDto.setStatus(OrderStatus.CART);
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setProductId(burger.getId());
		orderDto.setComment("I like my burger " + selected.getName());
		
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		assertThat(placedOrderDto.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrderDto.getOrderTime(), notNullValue());
		assertThat(placedOrderDto.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrderDto.getProductId(), equalTo(burger.getId()));
		assertThat(placedOrderDto.getChoices(), notNullValue());
		for (ChoiceDTO orderChoice : placedOrderDto.getChoices()) {
			for (ProductOption option : orderChoice.getOptions()) {
				if(option.getName() == selected.getName() )
					assertThat(option.getSelected(), equalTo(true));
			}
		}
		
		//#3 Check "getOrders"
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(business, checkIn.getKey(), "cart");
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(1));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
		
		//#4.1 Update order to placed
		placedOrderDto.setStatus(OrderStatus.PLACED);
		placedOrderDto = orderCtrl.updateOrder(business, placedOrder, placedOrderDto, checkIn);
		assertThat(placedOrderDto.getStatus(), is(OrderStatus.PLACED) );
		
		//#4.2 Confirm placed order
		placedOrderDto.setStatus(OrderStatus.RECEIVED);
		OrderDTO receivedOrder = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		assertThat(receivedOrder.getStatus(), is(OrderStatus.RECEIVED));
		
		CheckIn checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		assertThat(checkIn.getStatus(), is(CheckInStatus.CHECKEDIN));
		
		//#4.3 Try to set the status back to placed	
		OrderDTO result;
		try {
			placedOrderDto.setStatus(OrderStatus.PLACED);
			result = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		} catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
		}
		
		//#4.4 Cancel order
		placedOrderDto.setStatus(OrderStatus.CANCELED);
		result = orderCtrl.updateOrderForBusiness(business, placedOrder, placedOrderDto);
		assertThat(result.getStatus(), is(OrderStatus.CANCELED));
	}
	
}
