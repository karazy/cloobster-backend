package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.embedded.OrderStatus;
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

	@Before
	public void setUp() throws Exception {
		// Configure location to return euro currency.
		when(location.getCurrency()).thenReturn("EUR");
		
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
		when(bill.getCheckIn()).thenReturn(checkInKey);
		
		Iterable<Order> orders = mock(Iterable.class);
		Iterator<Order> ordersIterator = mock(Iterator.class);
		when(orders.iterator()).thenReturn(ordersIterator );
		// Configure the iterator to return true for the first check.
		// The next time return true for the for loop.
		// After that return false, we only want to return one Order.
		when(ordersIterator.hasNext()).thenReturn(true,true, false);
		Order placedOrder = mock(Order.class);
		when(placedOrder.getStatus()).thenReturn(OrderStatus.RECEIVED);
		when(ordersIterator.next()).thenReturn(placedOrder );
		
		when(orderRepo.belongingToLocationAndCheckIn(location, checkInKey)).thenReturn(orders );
		
		billCtrl.updateBill(location, bill , billData );
		
		verify(bill, never()).setCleared(true);
		verify(billRepo,never()).saveOrUpdate(bill);
	}
}
