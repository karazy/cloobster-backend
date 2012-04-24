package net.eatsense.controller.bill;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

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

/**
 * Contains setup and all test cases for createBill method.
 * 
 * @author Nils Weiher
 *
 */
public class CalculateTotalPriceTest {
	
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
		private BillRepository br;

		private CheckIn checkIn;
		private Business business;
		private SpotDTO spotDto;

		private Product frites;

		private Product burger;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		billCtrl = injector.getInstance(BillController.class);
		rr = injector.getInstance(BusinessRepository.class);
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
		frites = pr.getByProperty("name", "Pommes Frites");
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
		burger = pr.getByProperty("name", "Classic Burger");
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
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	@Test
	public void testCalculateTotal() {
	
		//Check calculateTotalPrice
		
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
	}
	
}
