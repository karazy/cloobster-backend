package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
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

	@Before
	public void setUp() throws Exception {
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
	
	
}
