package net.eatsense.controller;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
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
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.event.DeleteCheckInEvent;
import net.eatsense.event.MoveCheckInEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.exceptions.CheckInFailureException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.CheckInStatusDTO;

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
import com.googlecode.objectify.NotFoundException;

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
	
	@Mock
	private OrderChoiceRepository orderChoiceRepo;

	@Before
	public void setUp() throws Exception {
		ValidatorFactory avf =
	            Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory();
		ctr = new CheckInController(businessRepo, checkInRepo, spotRepo, transform, mapper, avf.getValidator(), requestRepo, orderRepo, orderChoiceRepo, eventBus);
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
		thrown.expectMessage("nickname");
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
		
		inOrder.verify(orderRepo).getKeysByProperty("checkIn", checkIn);
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
	
	@Test
	public void testDeleteCheckInInvalidBusiness() throws Exception {
		long checkInId = 1;
		
		CheckIn checkIn = mock(CheckIn.class);
		when(checkInRepo.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		@SuppressWarnings("unchecked")
		Key<Business> anotherBusiness = mock(Key.class);
		when(business.getKey()).thenReturn(anotherBusiness);
		
		thrown.expect(IllegalArgumentException.class);
		
		ctr.deleteCheckIn(business, checkInId);
	}
	
	@Test
	public void testDeleteCheckInUnknownId() throws Exception {
		long checkInId = 1;
		when(checkInRepo.getById(checkInId)).thenThrow(new NotFoundException(null));
		thrown.expect(IllegalArgumentException.class);
		
		ctr.deleteCheckIn(business, checkInId);
	}
	
	@Test
	public void testDeleteCheckInZeroId() throws Exception {
		long checkInId = 0;
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("checkInId");
		
		ctr.deleteCheckIn(business, checkInId);
	}
	
	@Test
	public void testDeleteCheckInNullBusinessId() throws Exception {
		long checkInId = 1;
		when(business.getId()).thenReturn(null);
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business id");
		ctr.deleteCheckIn(business, checkInId);
	}
	
	@Test
	public void testDeleteCheckInNullBusiness() throws Exception {
		long checkInId = 1;
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		ctr.deleteCheckIn(null, checkInId);
	}
	
	@Test
	public void testDeleteCheckIn() throws Exception {
		long checkInId = 1;
		int activeCheckIns = 2;
		List<Key<Order>> orderKeys = new ArrayList<Key<Order>>();
		@SuppressWarnings("unchecked")
		Key<Order> orderKey1 = mock( Key.class);
		orderKeys.add( orderKey1 );
		List<Key<OrderChoice>> choiceList = new ArrayList<Key<OrderChoice>>();
		List<Key<Request>> requestKeys = new ArrayList<Key<Request>>();
		
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(checkInId);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(business.getKey()).thenReturn(businessKey);
		when(checkInRepo.getById(checkInId)).thenReturn(checkIn);
		
		
		when(orderChoiceRepo.getKeysByParent(orderKey1)).thenReturn(choiceList );
		when(requestRepo.getKeysByProperty("checkIn", checkIn)).thenReturn(requestKeys );
		when(orderRepo.getKeysByProperty("checkIn", checkIn)).thenReturn(orderKeys );
		when(checkInRepo.countActiveCheckInsAtSpot(spotKey)).thenReturn(activeCheckIns);
		
		ctr.deleteCheckIn(business, checkInId);
		
		verify(requestRepo).delete(requestKeys);
		verify(orderRepo).delete(orderKeys);
		verify(orderChoiceRepo).delete(choiceList);
		verify(checkInRepo).delete(checkIn);
		
		ArgumentCaptor<DeleteCheckInEvent> eventArgument = ArgumentCaptor.forClass(DeleteCheckInEvent.class);
		verify(eventBus).post(eventArgument.capture());
		DeleteCheckInEvent newEvent = eventArgument.getValue();
		assertThat(newEvent.getCheckIn(), is(checkIn));
		assertThat(newEvent.getBusiness(), is(business));
		assertThat(newEvent.isCheckOut(), is(false));
		assertThat(newEvent.getCheckInCount().get(), is(activeCheckIns-1));
	}
	
	@Test
	public void testUpdateCheckInAsBusinessNullData() throws Exception {
		long checkInId = 1;
		CheckInStatusDTO checkInData = null;
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("checkInData");
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
	}
	
	@Test
	public void testUpdateCheckInAsBusinessNoUpdateData() throws Exception {
		long checkInId = 1;
		long newSpotId = 1;
		long spotId = 1;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		CheckIn checkIn = mock (CheckIn.class);
		
		checkInData.setSpotId(newSpotId);
		when(spotKey.getId()).thenReturn( spotId);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(checkInRepo.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(business.getKey()).thenReturn(businessKey);
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
		
		verify(checkInRepo, never()).saveOrUpdate(checkIn);
		verify(eventBus, never()).post(any(MoveCheckInEvent.class));
	}
	
	@Test
	public void testUpdateCheckInAsBusinessInvalidBusiness() throws Exception {
		long checkInId = 1;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		CheckIn checkIn = mock (CheckIn.class);
		@SuppressWarnings("unchecked")
		Key<Business> anotherBusiness = mock(Key.class);
		
		when(checkInRepo.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(business.getKey()).thenReturn(anotherBusiness);
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("checkIn");
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
	}
	
	@Test
	public void testUpdateCheckInAsBusinessUnknownId() throws Exception {
		long checkInId = 1;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		
		when(checkInRepo.getById(checkInId)).thenThrow(new NotFoundException(null));
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("checkInId");
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
	}
	
	@Test
	public void testUpdateCheckInAsBusinessZeroId() throws Exception {
		long checkInId = 0;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("checkInId");
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
	}
	
	@Test
	public void testUpdateCheckInAsBusinessNullBusinessId() throws Exception {
		long checkInId = 1;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		
		when(business.getId()).thenReturn(null);
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business id");
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);	
	}
	
	
	@Test
	public void testUpdateCheckInAsBusinessNullBusiness() throws Exception {
		long checkInId = 1;
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("business");
		
		ctr.updateCheckInAsBusiness(null, checkInId, checkInData);	
	}
	
	@Test
	public void testUpdateCheckInAsBusiness() throws Exception {
		long checkInId = 1;
		long newSpotId = 2;
		long spotId = 1;
		
		CheckInStatusDTO checkInData = new CheckInStatusDTO();
		checkInData.setSpotId( newSpotId );
		CheckIn checkIn = mock (CheckIn.class);
		@SuppressWarnings("unchecked")
		Key<Spot> newSpotKey= mock( Key.class);
		List<Request> requestList = new ArrayList<Request>();
		Request request = mock ( Request.class);
		requestList.add(request );
				
		when(spotKey.getId()).thenReturn( spotId);
		when(checkInRepo.getById(checkInId)).thenReturn(checkIn );
		when(checkIn.getBusiness()).thenReturn(businessKey);
		when(checkIn.getSpot()).thenReturn(spotKey);
		when(business.getKey()).thenReturn(businessKey);
		when(requestRepo.getListByProperty("checkIn", checkIn)).thenReturn(requestList );
		when(spotRepo.getKey(businessKey, newSpotId)).thenReturn(newSpotKey);
		
		ctr.updateCheckInAsBusiness(business, checkInId, checkInData);
		
		verify(request).setSpot(newSpotKey);
		verify(checkIn).setSpot(newSpotKey);
		verify(checkInRepo).saveOrUpdate(checkIn);
		verify(requestRepo).saveOrUpdate(requestList);
		
		ArgumentCaptor<MoveCheckInEvent> eventArgument = ArgumentCaptor.forClass(MoveCheckInEvent.class);
		verify(eventBus).post(eventArgument.capture());
		MoveCheckInEvent newEvent = eventArgument.getValue();
		assertThat(newEvent.getCheckIn(), is(checkIn));
		assertThat(newEvent.getBusiness(), is(business));
		assertThat(newEvent.getOldSpot(), is(spotKey));	
	}
}
