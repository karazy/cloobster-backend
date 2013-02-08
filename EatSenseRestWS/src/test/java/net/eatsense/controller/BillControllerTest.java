package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.List;
import java.util.Arrays;
import java.util.Iterator;

import net.eatsense.domain.Account;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.event.UpdateBillEvent;
import net.eatsense.exceptions.BillFailureException;
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

	@Before
	public void setUp() throws Exception {
		// Configure location to return euro currency.
		when(location.getCurrency()).thenReturn("EUR");
		
		when(productRepo.getByKey(productKey)).thenReturn(orderedProduct);
		
		when(checkIn.getSpot()).thenReturn(spotKey);
		
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
		when(ordersIterator.hasNext()).thenReturn(true,true, false);
		
		Order placedOrder = mock(Order.class);
		when(placedOrder.getStatus()).thenReturn(OrderStatus.RECEIVED);
		
		when(placedOrder.getProduct()).thenReturn(productKey  );
		
		when(ordersIterator.next()).thenReturn(placedOrder );
		
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
		
		thrown.expect(BillFailureException.class);
		thrown.expectMessage("business");
		
		billCtrl.createBill(location, checkIn, billData , false);
	}
}
