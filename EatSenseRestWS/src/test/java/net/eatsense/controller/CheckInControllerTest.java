package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.event.DeleteCheckInEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.exceptions.CheckInFailureException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.Transformer;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.eventbus.EventBus;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class CheckInControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private CheckInController ctr;
	@Mock
	private BusinessRepository businessRepo;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private CheckInRepository checkInRepo;
	@Mock
	private Transformer transform;
	@Mock
	private ObjectMapper mapper;
	@Mock
	private Business business;
	
	@Mock
	private RequestRepository requestRepo;
	@Mock
	private OrderRepository orderRepo;
	@Mock
	private EventBus eventBus;
	@Mock
	private Spot spot;
	@Mock
	Key<Business> businessKey;
	@Mock
	Key<Spot> spotKey;

	@Before
	public void setUp() throws Exception {
		ValidatorFactory avf =
	            Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory();
		ctr = new CheckInController(businessRepo, checkInRepo, spotRepo, transform, mapper, avf.getValidator(), requestRepo, orderRepo, eventBus);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCreateCheckInEmptyBarcode() {
		thrown.expect(IllegalArgumentException.class);
		String spotId = "";
	
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInNullBarcode() {
		thrown.expect(NullPointerException.class);
		String spotId = null;
	
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInNullDTO() {
		thrown.expect(NullPointerException.class);
		CheckInDTO checkIn = null;
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInInvalidStatus() {
		thrown.expect(IllegalArgumentException.class);
		String spotId = "b4rc0de";
	
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.CHECKEDIN);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInNullStatus() {
		thrown.expect(NullPointerException.class);
		String spotId = "b4rc0de";
	
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(null);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInUnknownBarcode() {
		thrown.expect(IllegalArgumentException.class);
		String spotId = "b4rc0de";
		
		when(business.getKey()).thenReturn( businessKey);
		when(spot.getBusiness()).thenReturn( businessKey);
		when(spot.getKey()).thenReturn(spotKey);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(new ArrayList<CheckIn>());
		
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.INTENT);
		ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInNicknameInUse() throws Exception {
		thrown.expect(CheckInFailureException.class);
		thrown.expectMessage("errormessage");
		String spotId = "b4rc0de";
		String nickname = "FakeNik";
		
		when(business.getKey()).thenReturn( businessKey);
		when(spot.getBusiness()).thenReturn( businessKey);
		when(spot.getKey()).thenReturn(spotKey);
		when(spotRepo.getByProperty("barcode", spotId)).thenReturn(spot);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		ArrayList<CheckIn> checkInList = new ArrayList<CheckIn>();
		CheckIn olderCheckIn = new CheckIn();
		olderCheckIn.setNickname(nickname);
		checkInList.add(olderCheckIn);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(checkInList);
		when(mapper.writeValueAsString(any())).thenReturn("errormessage");
		
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname(nickname);
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckInTooLongNickname() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("errormessage");
		String spotId = "b4rc0de";
		
		when(business.getKey()).thenReturn( businessKey);
		when(spot.getBusiness()).thenReturn( businessKey);
		when(spot.getKey()).thenReturn(spotKey);
		when(spotRepo.getByProperty("barcode", spotId)).thenReturn(spot);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(new ArrayList<CheckIn>());
		when(mapper.writeValueAsString(any())).thenReturn("errormessage");
		
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik1234567890123456789012345");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
	}
	
	@Test
	public void testCreateCheckIn() {	
		String spotId = "b4rc0de";
		
		when(business.getKey()).thenReturn( businessKey);
		when(spot.getBusiness()).thenReturn( businessKey);
		when(spot.getKey()).thenReturn(spotKey);
		when(spotRepo.getByProperty("barcode", spotId)).thenReturn(spot);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(new ArrayList<CheckIn>());
		
		CheckInDTO checkIn = new CheckInDTO();
		checkIn.setSpotId(spotId);
		checkIn.setNickname("FakeNik");
		checkIn.setStatus(CheckInStatus.INTENT);
		checkIn = ctr.createCheckIn( checkIn);
		assertThat(checkIn, notNullValue());
		assertThat(checkIn.getUserId(), notNullValue());
		
		ArgumentCaptor<CheckIn> checkInArgument = ArgumentCaptor.forClass(CheckIn.class);
		verify(checkInRepo).saveOrUpdate(checkInArgument.capture());
		CheckIn savedCheckIn = checkInArgument.getValue();
		
		assertThat(savedCheckIn.getStatus(), is(CheckInStatus.CHECKEDIN));
		assertThat(savedCheckIn.getSpot(), is(spotKey));
		assertThat(savedCheckIn.getBusiness(), is(businessKey));
		assertThat(savedCheckIn.getUserId(), is(checkIn.getUserId()));
		assertThat(savedCheckIn.getNickname(), is(checkIn.getNickname()));
		ArgumentCaptor<NewCheckInEvent> eventArgument = ArgumentCaptor.forClass(NewCheckInEvent.class);
		verify(eventBus).post(eventArgument.capture());
		NewCheckInEvent newEvent = eventArgument.getValue();
		assertThat(newEvent.getCheckIn(), is(savedCheckIn));
		assertThat(newEvent.getBusiness(), is(business));
	}
	
	@Test
	public void testGetSpotInformationUnknown() throws Exception {
		String barcode = "bla";
		assertThat(ctr.getSpotInformation(barcode), nullValue());
		verify(spotRepo).getByProperty("barcode", barcode);
	}
	
	@Test
	public void testGetSpotInformationEmptyBarcode() throws Exception {
		String barcode = null;
		assertThat(ctr.getSpotInformation(barcode), nullValue());
	}
	
	@Test
	public void testGetSpotInformationNullBarcode() throws Exception {
		String barcode = null;
		assertThat(ctr.getSpotInformation(barcode), nullValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSpotInformation() throws Exception {
		String barcode = "abarcode";

		when(spotRepo.getByProperty("barcode", barcode )).thenReturn(spot);
		when(spot.getBusiness()).thenReturn( businessKey);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		
		assertThat(ctr.getSpotInformation(barcode), notNullValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetOtherUsersAtSpotPaymentLinkPossible() throws Exception {
		String barcode = "abarcode";
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(spot.getBarcode()).thenReturn(barcode);
		when(spotRepo.getByKey(spotKey)).thenReturn(spot);
		
		ArrayList<CheckIn> checkInList = new ArrayList<CheckIn>();
		CheckIn olderCheckIn = new CheckIn();
		olderCheckIn.setNickname("one");
		olderCheckIn.setId(1l);
		checkInList.add(olderCheckIn);
		olderCheckIn = new CheckIn();
		olderCheckIn.setNickname("two");
		olderCheckIn.setId(2l);
		olderCheckIn.setStatus(CheckInStatus.PAYMENT_REQUEST);
		checkInList.add(olderCheckIn);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(checkInList);
		
		List<User> userList = ctr.getOtherUsersAtSpot(checkIn, barcode);
		
		assertThat(userList, hasSize(1));
		for (User user : userList) {
			assertThat(user.getNickname(), anyOf(is("one"), is("two")));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetOtherUsersAtSpotNullCheckIn() throws Exception {
		String barcode = "abarcode";
		CheckIn checkIn = null;
		assertThat(ctr.getOtherUsersAtSpot(checkIn, barcode).isEmpty(), is(true));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetOtherUsersAtSpot() throws Exception {
		String barcode = "abarcode";
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(spot.getBarcode()).thenReturn(barcode);
		when(spotRepo.getByKey(spotKey)).thenReturn(spot);
		
		ArrayList<CheckIn> checkInList = new ArrayList<CheckIn>();
		CheckIn olderCheckIn = new CheckIn();
		olderCheckIn.setNickname("one");
		olderCheckIn.setId(1l);
		checkInList.add(olderCheckIn);
		olderCheckIn = new CheckIn();
		olderCheckIn.setNickname("two");
		olderCheckIn.setId(2l);
		checkInList.add(olderCheckIn);
		when(checkInRepo.getBySpot(spotKey)).thenReturn(checkInList);
		
		List<User> userList = ctr.getOtherUsersAtSpot(checkIn, barcode);
		assertThat(userList, hasSize(2));
		for (User user : userList) {
			assertThat(user.getNickname(), anyOf(is("one"), is("two")));
		}
	}
	
	@Test
	public void testGetCheckInEmptyUid() throws Exception {
		String checkInUid = "";
		
		assertThat(ctr.getCheckIn(checkInUid), nullValue());
		
		verify(checkInRepo, never()).getByProperty(eq("userId"), anyString());
	}
	
	@Test
	public void testGetCheckInNullUid() throws Exception {
		String checkInUid = null;
		
		assertThat(ctr.getCheckIn(checkInUid), nullValue());
		
		verify(checkInRepo, never()).getByProperty(eq("userId"), anyString());
	}
	
	@Test
	public void testGetCheckIn() throws Exception {
		String checkInUid = "uniqueid";
		CheckIn checkIn = mock(CheckIn.class);
		when(checkInRepo.getByProperty("userId", checkInUid)).thenReturn(checkIn );
		
		assertThat(ctr.getCheckIn(checkInUid), is(checkIn));
	}
	
	@Test
	public void testCheckOutNullCheckInBusiness() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(null);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		
		ctr.checkOut(checkIn);
		verifyZeroInteractions(checkInRepo, orderRepo);
	}
	
	@Test
	public void testCheckOutNullCheckInSpot() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("spot");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(null);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutZeroCheckInId() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("id");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(0l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutNullCheckInId() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("id");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(null);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutCheckInStatusOrderPlaced() throws Exception {
		thrown.expect(CheckInFailureException.class);
		thrown.expectMessage("invalid status");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.ORDER_PLACED);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutCheckInStatusPaymentRequest() throws Exception {
		thrown.expect(CheckInFailureException.class);
		thrown.expectMessage("invalid status");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.PAYMENT_REQUEST);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutNullCheckInStatus() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("status");
		
		CheckIn checkIn = mock(CheckIn.class);
		
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(null);
		
		ctr.checkOut(checkIn);
	}
	
	@Test
	public void testCheckOutNullCheckIn() throws Exception {
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("checkIn");
		CheckIn checkIn = null;
		
		ctr.checkOut(checkIn);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCheckOut() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getStatus()).thenReturn(CheckInStatus.CHECKEDIN);
		when(businessRepo.getByKey(businessKey)).thenReturn(business);
		int activeCheckIns = 2;
		when(checkInRepo.countActiveCheckInsAtSpot(spotKey)).thenReturn(activeCheckIns);
		
		InOrder inOrder = inOrder(orderRepo);
		
		ctr.checkOut(checkIn);
		
		inOrder.verify(orderRepo).getKeysByProperty("status", OrderStatus.CART.toString());
		inOrder.verify(orderRepo).delete(anyCollection());
		verify(checkInRepo).delete(checkIn);
		
		ArgumentCaptor<DeleteCheckInEvent> eventArgument = ArgumentCaptor.forClass(DeleteCheckInEvent.class);
		verify(eventBus).post(eventArgument.capture());
		DeleteCheckInEvent newEvent = eventArgument.getValue();
		assertThat(newEvent.getCheckIn(), is(checkIn));
		assertThat(newEvent.getBusiness(), is(business));
		assertThat(newEvent.isCheckOut(), is(true));
		assertThat(newEvent.getCheckInCount().get(), is(activeCheckIns-1));
	}
}
