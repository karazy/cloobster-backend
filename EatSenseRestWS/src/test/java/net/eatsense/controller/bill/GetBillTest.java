package net.eatsense.controller.bill;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

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
import org.joda.money.CurrencyUnit;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GetBillTest {
	
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

	private BillDTO billData;

	private Integer billTotal;

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
		Product frites = pr.getByProperty("name", "Pommes Frites");
		OrderDTO orderDto = new OrderDTO();
		orderDto.setAmount(1);
		orderDto.setComment("I like fries!");
		orderDto.setProduct(transform.productToDto(frites));
		orderDto.setStatus(OrderStatus.CART);
		
		//#1 Place a simple order without choices...
		Long orderId = orderCtrl.placeOrderInCart(business, checkIn, orderDto);
		OrderDTO placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		Order placedOrder = orderCtrl.getOrder(business, orderId);
		
		billTotal = billCtrl.calculateTotalPrice(placedOrder, CurrencyUnit.EUR).getAmountMinorInt();
		
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
		
		placedOrderDto = orderCtrl.getOrderAsDTO(business, orderId);
		placedOrder = orderCtrl.getOrder(business, orderId);
		
		billTotal += billCtrl.calculateTotalPrice(placedOrder, CurrencyUnit.EUR).getAmountMinorInt();
		
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
		Bill bill = br.getById(business.getKey(), billData.getId());
		//#1 updateBill
		billData.setCleared(true);
		billCtrl.updateBill(business, bill, billData);
		
	}
	
	@Test
	public void testGetBillForCheckInUnknownBusinessId() {
		business.setId(12345l);
		assertThat(billCtrl.getBillForCheckIn(business, checkIn.getId()), nullValue());
	}
	
	@Test
	public void testGetBillForCheckInUnknownId() {
		assertThat(billCtrl.getBillForCheckIn(business, 1234), nullValue());
	}
	
	@Test(expected= NullPointerException.class)
	public void testGetBillForCheckInNullBusinessId() {
		business.setId(null);
		billCtrl.getBillForCheckIn(business, checkIn.getId());
	}
	
	@Test(expected= NullPointerException.class)
	public void testGetBillForCheckInNullBusiness() {
		billCtrl.getBillForCheckIn(null, checkIn.getId());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetBillForCheckInZeroId() {
		billCtrl.getBillForCheckIn(business, 0);
	}

	@Test
	public void testGetBillForCheckIn() {
		BillDTO bill = billCtrl.getBillForCheckIn(business, checkIn.getId());
		assertThat(bill.getCheckInId(), is(checkIn.getId()));
	}
	
	@Test(expected= NullPointerException.class)
	public void testGetBillNullBusiness() {
		billCtrl.getBill(null, billData.getId());
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testGetBillZeroId() {
		billCtrl.getBill(business, 0);
	}
	
	@Test
	public void testGetBillUnknownId() {
		assertThat(billCtrl.getBill(business, 1234), nullValue());
	}
	
	@Test(expected= NullPointerException.class)
	public void testGetBillNullBusinessId() {
		business = new Business();
		
		billCtrl.getBill(business, billData.getId());
	}
	
	@Test
	public void testGetBillUnknownBusinessId() {
		business = new Business();
		business.setId(12345l);
		assertThat(billCtrl.getBill(business, billData.getId()), nullValue());
	}

	@Test
	public void testGetBill() {
		Bill newBill = billCtrl.getBill(business, billData.getId());
		
		assertThat(newBill.getId(), is(billData.getId()));
		assertThat(newBill.getBusiness(), is(business.getKey()));
		assertThat(newBill.getCreationTime(), lessThan( new Date()));
		assertThat(newBill.getPaymentMethod(), is(billData.getPaymentMethod()));
		assertThat(newBill.getTotal(), is(billTotal));
	}
}
