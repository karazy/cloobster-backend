package net.eatsense.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.OrderDTO;
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

@RunWith(MockitoJUnitRunner.class)
public class NewOrderControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	OrderController ctrl;
	@Mock
	private OrderRepository orderRepo;
	@Mock
	private OrderChoiceRepository orderChoiceRepo;
	@Mock
	private ProductRepository productRepo;
	@Mock
	private LocationRepository businessRepo;
	@Mock
	private CheckInRepository checkInRepo;
	@Mock
	private ChoiceRepository choiceRepo;
	@Mock
	private RequestRepository requestRepo;
	@Mock
	private Transformer trans;
	@Mock
	private ValidationHelper validator;
	@Mock
	private EventBus eventBus;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private AreaRepository areaRepo;
	@Mock
	private Business business;
	@Mock
	private CheckIn checkIn;
	@Mock
	private Key<Business> businessKey;
	private Long businessId = 1l;
	@Mock
	private Key<Area> areaKey;
	
	@Before
	public void setUp() throws Exception {
		ctrl = new OrderController(orderRepo, orderChoiceRepo, productRepo, businessRepo, checkInRepo, choiceRepo, requestRepo, trans, validator, eventBus, spotRepo, areaRepo);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getArea()).thenReturn(areaKey);
		when(business.getKey()).thenReturn(businessKey);
		when(businessKey.getId()).thenReturn(businessId );
		when(business.getId()).thenReturn(businessId);
	}
	
	@Test
	public void testPlaceOrderInCarthWithNoOrderProduct() throws Exception {
		
		OrderDTO orderData = new OrderDTO();
		orderData.setStatus(OrderStatus.CART);
		Long productId = 2l;
		orderData.setProductId(productId);

		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		Area area = mock(Area.class);
		when(areaRepo.getByKey(areaKey)).thenReturn(area );
		Product product = mock(Product.class);
		when(product.isNoOrder()).thenReturn(true);
		when(productRepo.getById(businessKey, productId)).thenReturn(product );
		
		thrown.expect(ValidationException.class);
		
		ctrl.placeOrderInCart(business, checkIn, orderData );
	}
}
