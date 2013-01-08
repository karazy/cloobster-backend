package net.eatsense.controller.bill;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import net.eatsense.EatSenseDomainModule;
import net.eatsense.controller.BillController;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.util.DummyDataDumper;

import org.apache.bval.guice.ValidationModule;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
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
	    private LocationRepository rr;
	    private ProductRepository pr;
	    private DummyDataDumper ddd;
		private Transformer transform;
		private BillController billCtrl;

		private CheckIn checkIn;
		private Business business;
		private SpotDTO spotDto;

		private Product frites;

		private Product burger;

		private Order placedOrderBurger;

		private Order placedOrderFries;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		orderCtrl = injector.getInstance(OrderController.class);
		checkinCtrl = injector.getInstance(CheckInController.class);
		billCtrl = injector.getInstance(BillController.class);
		rr = injector.getInstance(LocationRepository.class);
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
		frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		ProductDTO fritesDto = transform.productToDto(frites);
		orderDto.setChoices(fritesDto.getChoices());
		orderDto.setProductId(frites.getId());
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		placedOrderFries = orderCtrl.getOrder(business, orderId);
		
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
		orderDto.setAmount(1);
		orderDto.setProductId(burger.getId());
		orderDto.setChoices(burgerDto.getChoices());
		orderDto.setComment("I like my burger " + selected.getName());
		
		orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		assertThat(orderId, notNullValue());
		
		placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		placedOrderBurger = orderCtrl.getOrder(business, orderId);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test(expected = NullPointerException.class)
	public void testCalculateTotalNullOrder() {
		billCtrl.calculateTotalPrice(null,null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testCalculateTotalNullOrderId() {
		placedOrderFries.setId(null);
		billCtrl.calculateTotalPrice(placedOrderFries, null);
	}

	
	@Test(expected = NullPointerException.class)
	public void testCalculateTotalNullOrderProduct() {
		placedOrderFries.setProduct(null);
		billCtrl.calculateTotalPrice(placedOrderFries, null);
	}
	
	@Test
	public void testCalculateTotalAmount() {
		placedOrderFries.setAmount(3);
		assertThat(billCtrl.calculateTotalPrice(placedOrderFries,CurrencyUnit.EUR), is( Money.of(CurrencyUnit.EUR, 1.5).multipliedBy(placedOrderFries.getAmount())));
	}
	
	@Test
	public void testCalculateTotalZeroAmount() {
		placedOrderFries.setAmount(0);
		assertThat(billCtrl.calculateTotalPrice(placedOrderFries,CurrencyUnit.EUR), is( Money.of(CurrencyUnit.EUR,0) ));
	}
		
	@Test
	public void testCalculateTotalSingleProduct() {
		assertThat(billCtrl.calculateTotalPrice(placedOrderFries,CurrencyUnit.EUR), is( Money.of(CurrencyUnit.EUR,1.5) ));
	}
	
	@Test
	public void testCalculateTotalProductWithChoices() {
		assertThat(billCtrl.calculateTotalPrice(placedOrderBurger,CurrencyUnit.EUR), is( Money.of(CurrencyUnit.EUR,10) ));
	}
	
}
