package net.eatsense.controller;

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

	@Before
	public void setUp() throws Exception {
		billCtrl = new BillController(rr, orderRepo, orderChoiceRepo, productRepo, checkInRepo, billRepo, transformer, eventBus, spotRepo, accountRepo, validator, areaRepo);
	}
	
	@Test
	public void testGetBillNullBusiness() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		billCtrl.getBill(null, 1);
	}
}
