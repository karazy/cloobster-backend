package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testPlaceAndUpdateOrder() {
		
		assertThat(checkInData.getUserId(), notNullValue());
				
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
		Long orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProduct().getId(), equalTo(frites.getId()));
		
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
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
		
		//#3 Check "getOrders"
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(checkInData.getBusinessId(), checkInData.getUserId(), null);
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(2));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
		
		//#4.1 Update order to placed
		placedOrder.setStatus(OrderStatus.PLACED);
		placedOrder = orderCtrl.updateOrder(checkInData.getBusinessId(), placedOrder.getId(), placedOrder, checkInData.getUserId());
		assertThat(placedOrder.getStatus(), is(OrderStatus.PLACED) );
		
		//#4.2 Try to update order after status was set to PLACED
		
		OrderDTO result;
		try {
			placedOrder.setStatus(OrderStatus.CART);
			result = orderCtrl.updateOrder(checkInData.getBusinessId(), placedOrder.getId(), placedOrder, checkInData.getUserId());
		} catch (Exception e) {
			assertThat(e, instanceOf(RuntimeException.class));
		}
	}
	
	@Test
	public void testDeleteOrder() {
		assertThat(checkInData.getUserId(), notNullValue());
		
				
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
		Long orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
		orderCtrl.deleteOrder(checkInData.getBusinessId(), placedOrder.getId(),checkInData.getUserId());
		
		List<Order> orders = orderCtrl.getOrders(checkInData.getBusinessId(), checkInData.getUserId(), null);
		List<OrderChoice> choices = ocr.getByParent(Order.getKey(Business.getKey(checkInData.getBusinessId()), orderId));
		assertThat(choices.isEmpty(), is(true));
		assertThat(orders.isEmpty(), is(true));
	}
	
	@Test public void testGetOrdersWithStatus() {
		assertThat(checkInData.getUserId(), notNullValue());
		
				
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
		Long orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
		assertThat(placedOrder.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(placedOrder.getOrderTime(), notNullValue());
		assertThat(placedOrder.getComment(), equalTo(orderDto.getComment() ));
		assertThat(placedOrder.getProduct().getId(), equalTo(frites.getId()));
		
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
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
		//#3 Check "getOrders"
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(checkInData.getBusinessId(), checkInData.getUserId(), "CART");
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
		orderDto.setProduct(transform.productToDto(frites));
		orderDto.setStatus(OrderStatus.CART);
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = transform.productToDto(burger);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		Long orderId = orderCtrl.placeOrderInCart(checkInData.getBusinessId(), checkInData.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkInData.getBusinessId(), orderId);
		
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
		
		//#3 Check "getOrders"
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(checkInData.getBusinessId(), checkInData.getUserId(), null);
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(1));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
		
		//#4.1 Update order to placed
		placedOrder.setStatus(OrderStatus.PLACED);
		placedOrder = orderCtrl.updateOrder(checkInData.getBusinessId(), placedOrder.getId(), placedOrder, checkInData.getUserId());
		assertThat(placedOrder.getStatus(), is(OrderStatus.PLACED) );
		
		//#4.2 Confirm placed order
		placedOrder.setStatus(OrderStatus.RECEIVED);
		OrderDTO receivedOrder = orderCtrl.updateOrderForBusiness(checkInData.getBusinessId(), placedOrder.getId(), placedOrder);
		assertThat(receivedOrder.getStatus(), is(OrderStatus.RECEIVED));
		
		CheckIn checkIn = checkinCtrl.getCheckIn(checkInData.getUserId());
		assertThat(checkIn.getStatus(), is(CheckInStatus.CHECKEDIN));
		
		//#4.3 Try to set the status back to placed	
		OrderDTO result;
		try {
			placedOrder.setStatus(OrderStatus.PLACED);
			result = orderCtrl.updateOrderForBusiness(checkInData.getBusinessId(), placedOrder.getId(), placedOrder);
		} catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
		}
		
		//#4.4 Cancel order
		placedOrder.setStatus(OrderStatus.CANCELED);
		result = orderCtrl.updateOrderForBusiness(checkInData.getBusinessId(), placedOrder.getId(), placedOrder);
		assertThat(result.getStatus(), is(OrderStatus.CANCELED));
	}
	
}
