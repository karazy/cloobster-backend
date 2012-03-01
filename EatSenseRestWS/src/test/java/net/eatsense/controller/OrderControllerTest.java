package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.OrderStatus;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
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
	    private RestaurantRepository rr;
	    private MenuRepository mr;
	    private ProductRepository pr;
	    private ChoiceRepository cr;
	    private OrderRepository or;
	    private DummyDataDumper ddd;

		private SpotRepository br;

		private Transformer transform;

		private OrderChoiceRepository ocr;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		or = injector.getInstance(OrderRepository.class);
		ocr = injector.getInstance(OrderChoiceRepository.class);
		transform = injector.getInstance(Transformer.class);
		
		
		
		ddd= new DummyDataDumper(rr, br, mr, pr, cr);
		
		ddd.generateDummyRestaurants();
		
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testPlaceOrder() {
		// Do a checkin ...
		CheckInDTO checkIn = new CheckInDTO();
		SpotDTO spotDto = checkinCtrl.getSpotInformation("serg2011");
		checkIn.setNickname("PlaceOrderTest");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn.setSpotId("serg2011");
		checkIn.setUserId(checkinCtrl.createCheckIn( checkIn).getUserId() );
		checkIn.setRestaurantId(spotDto.getRestaurantId());
		
		
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
		Long orderId = orderCtrl.placeOrder(checkIn.getRestaurantId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		OrderDTO placedOrder = orderCtrl.getOrderAsDTO(checkIn.getRestaurantId(), orderId);
		
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
		
		orderId = orderCtrl.placeOrder(checkIn.getRestaurantId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrder = orderCtrl.getOrderAsDTO(checkIn.getRestaurantId(), orderId);
		
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
		
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDTO(checkIn.getRestaurantId(), checkIn.getUserId(), null);
		assertThat(orders, notNullValue());
		assertThat(orders.size(), equalTo(2));
		for (OrderDTO dto : orders) {
			assertThat(dto.getStatus(), equalTo(OrderStatus.CART));
		}
	}
	
}
