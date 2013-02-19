package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.event.NewBillEvent;
import net.eatsense.event.UpdateBillEvent;
import net.eatsense.exceptions.BillFailureException;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.validation.ValidationHelper;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.eventbus.EventBus;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class BillControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	BillController billCtrl;
	
	@Mock
	private RequestRepository rr;
	@Mock
	private OrderRepository orderRepo;
	@Mock
	private OrderChoiceRepository orderChoiceRepo;
	@Mock
	private ProductRepository productRepo;
	@Mock
	private CheckInRepository checkInRepo;
	@Mock
	private BillRepository billRepo;
	@Mock
	private Transformer transformer;
	@Mock
	private EventBus eventBus;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private AccountRepository accountRepo;
	@Mock
	private ValidationHelper validator;
	@Mock
	private AreaRepository areaRepo;
	@Mock
	private Business location;
	@Mock
	private Key<Business> locationKey;

	@Mock
	private Key<CheckIn> checkInKey;

	@Mock
	private Key<Product> productKey;

	@Mock
	private Product orderedProduct;

	@Mock
	private CheckIn checkIn;

	@Mock
	private Key<Spot> spotKey;

	@Mock
	private Key<Account> accountKey;

	@Mock
	private Account account;

	private Long locationId = 1l;

	@Mock
	private Spot spot;

	@Mock
	private Key<Area> areaKey;

	@Before
	public void setUp() throws Exception {
		// Configure location to return euro currency.
		when(location.getCurrency()).thenReturn("EUR");
		when(location.getKey()).thenReturn(locationKey);
		
		when(productRepo.getByKey(productKey)).thenReturn(orderedProduct);
		
		when(checkIn.getKey()).thenReturn(checkInKey);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getArea()).thenReturn(areaKey);
		
		when(checkInRepo.getByKey(checkInKey)).thenReturn(checkIn);
		
		when(accountRepo.getByKey(accountKey)).thenReturn(account);
		
		billCtrl = new BillController(rr, orderRepo, orderChoiceRepo, productRepo, checkInRepo, billRepo, transformer, eventBus, spotRepo, accountRepo, validator, areaRepo);
	}
	
	@Test
	public void testGetBillWithNullBusiness() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		billCtrl.getBill(null, 1);
	}
	
	@Test
	public void testGetBillWithZeroBillId() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("billId");
		
		Business business = mock(Business.class);
		billCtrl.getBill(business , 0);
	}
	
	@Test
	public void testGetBillNotFound() throws Exception {
		long billId = 1;
		
		when(location.getKey()).thenReturn(locationKey );
		when(billRepo.getById(locationKey, billId)).thenThrow(new NotFoundException(null));
		
		assertThat(billCtrl.getBill(location , billId ), nullValue());
	}
	
	@Test
	public void testGetBillForCheckInWithNullBusiness() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		billCtrl.getBillForCheckIn(null, 1);
	}
	
	@Test
	public void testGetBillForCheckInWithCheckInIdZero() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("checkInId");
		
		billCtrl.getBillForCheckIn(location, 0);
	}
	
	@Test
	public void testGetBillForCheckInNotFound() throws Exception {
		thrown.expect(net.eatsense.exceptions.NotFoundException.class);
		
		billCtrl.getBillForCheckIn(location, 1);
	}
	
	@Test	
	public void testGetBillForCheckIn() throws Exception {
		Bill bill = mock(Bill.class);
		
		long checkInId = 1;
		when(billRepo.belongingToCheckInAndLocation(location, checkInId )).thenReturn(bill);
		
		assertThat(billCtrl.getBillForCheckIn(location, checkInId), is(bill));
	}
	
	@Test
	public void testUpdateBillWithNullBusiness() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		Bill bill = mock(Bill.class);
		BillDTO billData = new BillDTO();
		billCtrl.updateBill(null, bill , billData );
	}
	
	@Test
	public void testUpdateBillWithNullBill() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("bill");
		
		BillDTO billData = new BillDTO();
		billCtrl.updateBill(location, null , billData );
	}
	
	@Test
	public void testUpdateBillWithInvalidBillData() throws Exception {
		thrown.expect(ValidationException.class);
		thrown.expectMessage("cleared");
		
		BillDTO billData = new BillDTO();
		billData.setCleared(false);
		Bill bill = mock(Bill.class);
		
		billCtrl.updateBill(location, bill , billData );
	}
	
	@Test
	public void testUpdateBillAlreadyCleared() throws Exception {
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("cleared");
		
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		Bill bill = mock(Bill.class);
		
		when(bill.isCleared()).thenReturn(true);
		
		billCtrl.updateBill(location, bill , billData );
	}
	
	@Test
	public void testUpdateBillWithNoOrders() throws Exception {
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("orders");
		
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill bill = mock(Bill.class);
		when(bill.getCheckIn()).thenReturn(checkInKey);
		
		Iterable<Order> orders = mock(Iterable.class);
		Iterator<Order> ordersIterator = mock(Iterator.class);
		when(orders.iterator()).thenReturn(ordersIterator );
		
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orders );
		
		billCtrl.updateBill(location, bill , billData );
	}
	
	@Test
	public void testUpdateBillWithPlacedOrder() throws Exception {
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("unconfirmed orders");
		
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill bill = mock(Bill.class);
		when(bill.getCheckIn()).thenReturn(checkInKey);
		
		Iterable<Order> orders = mock(Iterable.class);
		Iterator<Order> ordersIterator = mock(Iterator.class);
		when(orders.iterator()).thenReturn(ordersIterator );
		// Configure the iterator to return true for the first check.
		// The next time return true for the for loop.
		// After that return false, we only want to return one Order.
		when(ordersIterator.hasNext()).thenReturn(true,true, false);
		Order placedOrder = mock(Order.class);
		when(placedOrder.getStatus()).thenReturn(OrderStatus.PLACED);
		when(ordersIterator.next()).thenReturn(placedOrder );
		
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orders );
		
		billCtrl.updateBill(location, bill , billData );
	}
	
	@Test
	public void testUpdateBillWithNoConfirmedOrders() throws Exception {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill bill = mock(Bill.class);
		when(bill.getCheckIn()).thenReturn(checkInKey);
		
		Iterable<Order> orders = mock(Iterable.class);
		Iterator<Order> ordersIterator = mock(Iterator.class);
		when(orders.iterator()).thenReturn(ordersIterator );
		// Configure the iterator to return true for the first check.
		// The next time return true for the for loop.
		// After that return false, we only want to return one Order.
		when(ordersIterator.hasNext()).thenReturn(true,true, false);
		Order placedOrder = mock(Order.class);
		when(placedOrder.getStatus()).thenReturn(OrderStatus.CART);
		when(ordersIterator.next()).thenReturn(placedOrder );
		
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orders );
		
		billCtrl.updateBill(location, bill , billData );
		
		verify(bill, never()).setCleared(true);
		verify(billRepo,never()).saveOrUpdate(bill);
	}
	
	@Test
	public void testUpdateBill() throws Exception {
		BillDTO billData = new BillDTO();
		billData.setCleared(true);
		
		Bill bill = mock(Bill.class);
		Key<Bill> billKey = mock(Key.class);
		when(bill.getKey()).thenReturn(billKey);
		when(bill.getCheckIn()).thenReturn(checkInKey);
		
		when(account.getActiveCheckIn()).thenReturn(checkInKey);
		when(checkIn.getAccount()).thenReturn(accountKey);
		
		@SuppressWarnings("unchecked")
		Iterable<Order> orders = mock(Iterable.class);
		@SuppressWarnings("unchecked")
		Iterator<Order> ordersIterator = mock(Iterator.class);
		when(orders.iterator()).thenReturn(ordersIterator );
		// Configure the iterator to return true for the first check.
		// The next time return true for the for loop.
		// After that return false, we only want to return one Order.
		when(ordersIterator.hasNext()).thenReturn(true,true, true, false);
		
		Order placedOrder = mock(Order.class);
		Order cartOrder = mock(Order.class);
		when(cartOrder.getStatus()).thenReturn(OrderStatus.CART);
		when(placedOrder.getStatus()).thenReturn(OrderStatus.RECEIVED);
		
		when(placedOrder.getProduct()).thenReturn(productKey  );
		
		when(ordersIterator.next()).thenReturn(cartOrder, placedOrder );
		
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orders );
		
		@SuppressWarnings("unchecked")
		Iterable<Request> requests = mock(Iterable.class);
		Iterator<Request> requestsIterator = mock(Iterator.class);
		when(requests.iterator()).thenReturn(requestsIterator );
		when(requestsIterator.hasNext()).thenReturn(true, true, false);
		Request anotherRequest = mock(Request.class);
		Request requestOfCheckin = mock(Request.class);
		when(requestOfCheckin.getCheckIn()).thenReturn(checkInKey);
		Key<CheckIn> anotherCheckInKey = mock(Key.class);
		when(anotherRequest.getCheckIn()).thenReturn(anotherCheckInKey );
		String anotherRequestStatus = "ORDER_PLACED";
		when(anotherRequest.getStatus()).thenReturn(anotherRequestStatus );
		when(requestsIterator.next()).thenReturn(requestOfCheckin, anotherRequest);
		
		when(rr.belongingToSpotOrderedByReceivedTime(spotKey)).thenReturn(requests );
		
		billCtrl.updateBill(location, bill , billData );
		
		verify(checkIn).setStatus(CheckInStatus.COMPLETE);
		verify(checkIn).setArchived(true);
		verify(checkInRepo).saveOrUpdate(checkIn);
		
		verify(bill).setTotal(anyLong());
		verify(bill).setCleared(true);
		verify(billRepo).saveOrUpdate(bill);
		
		verify(placedOrder).setStatus(OrderStatus.COMPLETE);		
		verify(placedOrder).setBill(billKey);
		
		// Verify we dont touch cart orders
		verify(cartOrder, never()).setStatus(any(OrderStatus.class));

		ArgumentCaptor<java.util.List> orderListCaptor = ArgumentCaptor.forClass(java.util.List.class);
		
		verify(orderRepo).saveOrUpdateAsync(orderListCaptor.capture());
		assertThat(orderListCaptor.getValue().contains(placedOrder), is(true));
		
		verify(account).setActiveCheckIn(null);
		verify(accountRepo).saveOrUpdate(account);
		
		ArgumentCaptor<UpdateBillEvent> updateEventCaptor = ArgumentCaptor.forClass(UpdateBillEvent.class);
		
		verify(eventBus).post(updateEventCaptor.capture());
		UpdateBillEvent updateBillEvent = updateEventCaptor.getValue();
		
		assertThat(updateBillEvent.getBill(), is(bill));
		assertThat(updateBillEvent.getBusiness(), is(location));
		assertThat(updateBillEvent.getCheckIn(), is(checkIn));
		assertThat(updateBillEvent.getNewSpotStatus().get(), is(anotherRequestStatus));
	}
	
	@Test
	public void testCreateBillWithNullBusines() throws Exception {
		BillDTO billData = new BillDTO();
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		billCtrl.createBill(null, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithNullCheckIn() throws Exception {
		BillDTO billData = new BillDTO();
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("checkIn");
		
		billCtrl.createBill(location, null, billData , false);
	}
	
	@Test
	public void testCreateBillWithNullBillData() throws Exception {
		BillDTO billData = null;
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("billData");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithInvalidBillData() throws Exception {
		BillDTO billData = new BillDTO();
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("billData");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithInvalidBusiness() throws Exception {
		BillDTO billData = new BillDTO();
		billData.setPaymentMethod(new PaymentMethod("Bar"));
		
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("business");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithNoMatchingPaymentMethod() throws Exception {
		BillDTO billData = new BillDTO();
		billData.setPaymentMethod(new PaymentMethod("Bar"));
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(new PaymentMethod("NotBar"));
		
		when(location.getPaymentMethods()).thenReturn(paymentMethods );
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("payment");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithInvalidCheckInStatus() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		when(location.getPaymentMethods()).thenReturn(paymentMethods );
		
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("checkin status");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithInvalidBusinessId() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		when(location.getPaymentMethods()).thenReturn(paymentMethods );
		
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(checkIn.getBusiness()).thenReturn(locationKey);
		when(locationKey.getId()).thenReturn(locationId);
		
		thrown.expect(ValidationException.class);
		thrown.expectMessage("checkin");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithBasicBusiness() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		when(location.getPaymentMethods()).thenReturn(paymentMethods );
		
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(checkIn.getBusiness()).thenReturn(locationKey);
		
		when(locationKey.getId()).thenReturn(locationId);
		when(location.getId()).thenReturn(locationId);
		when(location.isBasic()).thenReturn(true);
		
		thrown.expect(IllegalAccessException.class);
		thrown.expectMessage("basic");
		
		billCtrl.createBill(location, checkIn, billData , false);

	}
	
	@Test
	public void testCreateBillWithWelcomeSpot() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(checkIn.getBusiness()).thenReturn(locationKey);
		
		when(locationKey.getId()).thenReturn(locationId);
		when(location.getId()).thenReturn(locationId);
		when(location.getPaymentMethods()).thenReturn(paymentMethods );

		when(spotRepo.getByKey(spotKey)).thenReturn(spot);
		when(spot.isWelcome()).thenReturn(true);
		
		thrown.expect(IllegalAccessException.class);
		thrown.expectMessage("welcome");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBillWithNoOrdersToBill() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(checkIn.getBusiness()).thenReturn(locationKey);
		
		when(locationKey.getId()).thenReturn(locationId);
		when(location.getId()).thenReturn(locationId);
		when(location.getPaymentMethods()).thenReturn(paymentMethods );

		when(spotRepo.getByKey(spotKey)).thenReturn(spot);
		
		java.util.List<Order> orderList = new ArrayList<Order>();
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orderList );
		
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("no orders");
	
		billCtrl.createBill(location, checkIn, billData , false);
	}
	
	@Test
	public void testCreateBill() throws Exception {
		BillDTO billData = new BillDTO();
		PaymentMethod paymentMethod = new PaymentMethod("Bar");
		billData.setPaymentMethod(paymentMethod);
		
		java.util.List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		paymentMethods.add(paymentMethod);
		
		
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(checkIn.getBusiness()).thenReturn(locationKey);
		
		when(locationKey.getId()).thenReturn(locationId);
		when(location.getId()).thenReturn(locationId);
		when(location.getPaymentMethods()).thenReturn(paymentMethods );

		String spotName = "spot";
		when(spot.getName()).thenReturn(spotName );
		when(spotRepo.getByKey(spotKey)).thenReturn(spot);
		
		java.util.List<Order> orderList = new ArrayList<Order>();
		Order order1 = mock(Order.class);
		when(order1.getStatus()).thenReturn(OrderStatus.RECEIVED);
		orderList.add(order1 );
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orderList );
		
		Bill newBill = new Bill();
		when(billRepo.newEntity()).thenReturn(newBill );
		
		Area area = mock(Area.class);
		String areaName = "area";
		when(area.getName()).thenReturn(areaName );
		when(areaRepo.getByKey(areaKey )).thenReturn(area );
		
		when(rr.getIdOfOldestRequestBelongingToSpot(spotKey)).thenReturn(null);
		
		billCtrl.createBill(location, checkIn, billData , false);
		
		assertThat(newBill.getPaymentMethod(), is(billData.getPaymentMethod()));
		assertThat(newBill.getBusiness(), is(locationKey));
		assertThat(newBill.getCheckIn(), is(checkInKey));
		assertThat(newBill.getCreationTime(), notNullValue());
		assertThat(newBill.isCleared(), is(false));
		assertThat(newBill.getSpot(), is(spotKey));
		assertThat(newBill.getSpotName(), is(spotName));
		assertThat(newBill.getArea(), is(areaKey));
		assertThat(newBill.getAreaName(), is(areaName));
		
		verify(billRepo).saveOrUpdate(newBill);
		
		ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
		verify(rr).saveOrUpdate(requestCaptor.capture());
		Request newRequest = requestCaptor.getValue();
		
		assertThat(newRequest.getObjectText(), is(paymentMethod.getName()));
		assertThat(newRequest.getStatus(), is(CheckInStatus.PAYMENT_REQUEST.toString()));
		
		verify(checkIn).setStatus(CheckInStatus.PAYMENT_REQUEST);
		verify(checkInRepo).saveOrUpdate(checkIn);
		
		ArgumentCaptor<NewBillEvent> newBillEventCaptor = ArgumentCaptor.forClass(NewBillEvent.class);
		
		verify(eventBus).post(newBillEventCaptor.capture());
		NewBillEvent newBillEvent = newBillEventCaptor.getValue();
		assertThat(newBillEvent.getBill(), is(newBill));
		assertThat(newBillEvent.getBusiness(), is(location));
		assertThat(newBillEvent.getCheckIn(), is(checkIn));
		assertThat(newBillEvent.getNewSpotStatus().get(), is(newRequest.getStatus()));
	}
	
	@Test
	public void testCalculateTotalPriceWithNullOrder() throws Exception {
		Order order  = null;
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("order");

		billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
	}
	
	@Test
	public void testCalculateTotalPriceWithNullChoices() throws Exception {
		Order order  = mock(Order.class);
		Long productPrice = 100l;
		when(order.getProductPrice()).thenReturn(productPrice );
		int amount = 2;
		when(order.getAmount()).thenReturn(amount );
		Key<Object> orderKey = mock(Key.class);
		
		java.util.List<OrderChoice> orderChoices = new ArrayList<OrderChoice>();
		when(orderChoiceRepo.getByParent(orderKey )).thenReturn(orderChoices );
		
		Money price = billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
		
		assertThat(price.getAmountMinorLong(), is(productPrice * amount));
	}
	
	@Test
	public void testCalculateTotalPriceWithOverrideSinglePrice() throws Exception {
		Order order  = mock(Order.class);
		Long productPrice = 100l;
		when(order.getProductPrice()).thenReturn(productPrice );
		int amount = 2;
		when(order.getAmount()).thenReturn(amount );
		Key<Order> orderKey = mock(Key.class);
		when(order.getKey()).thenReturn(orderKey);
		
		java.util.List<OrderChoice> orderChoices = new ArrayList<OrderChoice>();
		OrderChoice orderChoice = new OrderChoice();
		Choice choice = new Choice();
		choice.setIncludedChoices(0);
		choice.setPrice(100l);
		choice.setOverridePrice(ChoiceOverridePrice.OVERRIDE_SINGLE_PRICE);
		ProductOption productOption = new ProductOption("1", 1.5);
		productOption.setSelected(true);
		choice.setOptions(Arrays.asList(productOption, productOption));
		
		orderChoice.setChoice(choice );
		orderChoices.add(orderChoice);
		when(orderChoiceRepo.getByParent(orderKey )).thenReturn(orderChoices );
		
		Money price = billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
		
		assertThat(price.getAmountMinorLong(), is((productPrice + choice.getPrice() * choice.getOptions().size())  * amount));
	}
	
	@Test
	public void testCalculateTotalPriceWithOverrideFixedSum() throws Exception {
		Order order  = mock(Order.class);
		Long productPrice = 100l;
		when(order.getProductPrice()).thenReturn(productPrice );
		int amount = 2;
		when(order.getAmount()).thenReturn(amount );
		Key<Order> orderKey = mock(Key.class);
		when(order.getKey()).thenReturn(orderKey);
		
		java.util.List<OrderChoice> orderChoices = new ArrayList<OrderChoice>();
		OrderChoice orderChoice = new OrderChoice();
		Choice choice = new Choice();
		choice.setIncludedChoices(0);
		choice.setPrice(100l);
		choice.setOverridePrice(ChoiceOverridePrice.OVERRIDE_FIXED_SUM);
		ProductOption productOption = new ProductOption("1", 1.5);
		ProductOption productOption2 = new ProductOption("1", 0.5);
		productOption.setSelected(true);
		productOption2.setSelected(true);
		choice.setOptions(Arrays.asList(productOption, productOption));
		
		orderChoice.setChoice(choice );
		orderChoices.add(orderChoice);
		when(orderChoiceRepo.getByParent(orderKey )).thenReturn(orderChoices );
		
		Money price = billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
		
		assertThat(price.getAmountMinorLong(), is((productPrice + choice.getPrice())  * amount));
	}
	
	@Test
	public void testCalculateTotalPriceWithNoOverride() throws Exception {
		Order order  = mock(Order.class);
		Long productPrice = 100l;
		when(order.getProductPrice()).thenReturn(productPrice );
		int amount = 2;
		when(order.getAmount()).thenReturn(amount );
		Key<Order> orderKey = mock(Key.class);
		when(order.getKey()).thenReturn(orderKey);
		
		java.util.List<OrderChoice> orderChoices = new ArrayList<OrderChoice>();
		OrderChoice orderChoice = new OrderChoice();
		Choice choice = new Choice();
		choice.setIncludedChoices(0);
		choice.setPrice(100l);
		choice.setOverridePrice(ChoiceOverridePrice.NONE);
		ProductOption productOption = new ProductOption("1", 1.5);
		ProductOption productOption2 = new ProductOption("1", 0.5);
		productOption.setSelected(true);
		productOption2.setSelected(true);
		choice.setOptions(Arrays.asList(productOption, productOption2));
		
		orderChoice.setChoice(choice );
		orderChoices.add(orderChoice);
		when(orderChoiceRepo.getByParent(orderKey )).thenReturn(orderChoices );
		
		Money price = billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
		
		assertThat(price.getAmountMinorLong(), is((productPrice + productOption.getPriceMinor() + productOption2.getPriceMinor())  * amount));
	}
	
	@Test
	public void testCalculateTotalPriceWithIncludedOptions() throws Exception {
		Order order  = mock(Order.class);
		Long productPrice = 100l;
		when(order.getProductPrice()).thenReturn(productPrice );
		int amount = 2;
		when(order.getAmount()).thenReturn(amount );
		Key<Order> orderKey = mock(Key.class);
		when(order.getKey()).thenReturn(orderKey);
		
		java.util.List<OrderChoice> orderChoices = new ArrayList<OrderChoice>();
		OrderChoice orderChoice = new OrderChoice();
		Choice choice = new Choice();
		choice.setIncludedChoices(1);
		choice.setPrice(100l);
		choice.setOverridePrice(ChoiceOverridePrice.NONE);
		ProductOption productOption = new ProductOption("1", 1.5);
		ProductOption productOption2 = new ProductOption("1", 0.5);
		productOption.setSelected(true);
		productOption2.setSelected(true);
		choice.setOptions(Arrays.asList(productOption, productOption2));
		
		orderChoice.setChoice(choice );
		orderChoices.add(orderChoice);
		when(orderChoiceRepo.getByParent(orderKey )).thenReturn(orderChoices );
		
		Money price = billCtrl.calculateTotalPrice(order, CurrencyUnit.EUR);
		
		assertThat(price.getAmountMinorLong(), is((productPrice + productOption2.getPriceMinor())  * amount));
	}
	
	
}
