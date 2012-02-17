package net.eatsense.controller;

import java.util.List;

import net.eatsense.EatSenseDomainModule;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.CheckInRepository;
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
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

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

		private MenuController menuCtrl;

		private OrderChoiceRepository ocr;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		menuCtrl = injector.getInstance(MenuController.class);
		rr = injector.getInstance(RestaurantRepository.class);
		pr = injector.getInstance(ProductRepository.class);
		mr = injector.getInstance(MenuRepository.class);
		cr = injector.getInstance(ChoiceRepository.class);
		br = injector.getInstance(SpotRepository.class);
		or = injector.getInstance(OrderRepository.class);
		ocr = injector.getInstance(OrderChoiceRepository.class);
		
		
		
		
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
		CheckInDTO checkIn = checkinCtrl.checkInIntent("serg2011");
		checkIn.setNickname("PlaceOrderTest");
		checkIn = checkinCtrl.checkIn(checkIn.getUserId(), checkIn);
		
		// Should be checked in
		assertThat(checkIn.getStatus(), equalTo(CheckInStatus.CHECKEDIN.toString()) );
		
		// Get a product from the store.
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		orderDto.setProduct(menuCtrl.transformtoDto(frites, false));
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrder(checkIn.getRestaurantId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		Order order = or.getByKey(Restaurant.getKey(checkIn.getRestaurantId()), orderId);
		
		assertThat(order.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(order.getOrderTime(), notNullValue());
		assertThat(order.getComment(), equalTo(orderDto.getComment() ));
		assertThat(order.getProduct(), equalTo(frites.getKey()));
		
		
		//#2 Place an order with choices
		Product burger = pr.getByProperty("name", "Classic Burger");
		ProductDTO burgerDto = menuCtrl.transformtoDto(burger, false);
		
		ProductOption selected = burgerDto.getChoices().iterator().next().getOptions().iterator().next();
		selected.setSelected(true);
		
		orderDto.setId(null);
		orderDto.setAmount(2);
		orderDto.setProduct(burgerDto);
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrder(checkIn.getRestaurantId(), checkIn.getUserId(), orderDto);
		assertThat(orderId, notNullValue());
		
		order = or.getByKey(Restaurant.getKey(checkIn.getRestaurantId()), orderId);
		
		assertThat(order.getAmount(), equalTo(orderDto.getAmount()));
		assertThat(order.getOrderTime(), notNullValue());
		assertThat(order.getComment(), equalTo(orderDto.getComment() ));
		assertThat(order.getProduct(), equalTo(burger.getKey()));
		List<OrderChoice> orderchoices = or.getChildren(OrderChoice.class, order.getKey());
		assertThat(orderchoices, notNullValue());
		for (OrderChoice orderChoice : orderchoices) {
			if(orderChoice.getSelectedOptions()!= null) {
				assertThat(orderChoice.getSelectedOptions().get(0).getSelected(), equalTo(true));
				assertThat(orderChoice.getSelectedOptions().get(0).getName(), equalTo(selected.getName()));
			}
		}				
	}
	
}
