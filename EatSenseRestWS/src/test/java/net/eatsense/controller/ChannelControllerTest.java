package net.eatsense.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.cockpit.MessageDTO;

import org.apache.bval.guice.ValidationModule;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.channel.ChannelServicePb.SendMessageRequest;
import com.google.appengine.tools.development.testing.LocalChannelServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class ChannelControllerTest {
	
    private Injector injector;
    private ChannelController ctr;
    @Mock
    private BusinessRepository rr;
    private SpotRepository br;
    
    @Mock
    private CheckInRepository cr;
    
    @Mock
    private ChannelService channelService;

	private Business business;

	private CheckInController checkInCtrl;

	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = new ChannelController(rr, cr, injector.getInstance(ObjectMapper.class), channelService);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateCustomerChannelNullCheckIn() {
		ctr.createCustomerChannel(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateCustomerChannelNullCheckInId() {
		CheckIn checkIn = new CheckIn();
		
		ctr.createCustomerChannel(checkIn);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testCreateCustomerChannelTimeoutTooGreat() {
		//#1 Create a checkin ...
		CheckIn checkIn = mock(CheckIn.class);
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn, Optional.fromNullable(24*60 +1));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test
	public void testCreateCustomerChannelTimeoutMax() {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
		when(channelService.createChannel(anyString(), anyInt())).thenReturn("newclienttoken");
		
		//#2 Request token with valid uid ...
		Optional<Integer> timeout = Optional.fromNullable(24*60-1);
		String result = ctr.createCustomerChannel(checkIn, timeout);
		verify(channelService).createChannel(anyString(), eq(timeout.get()));
		
		assertThat(result, is("newclienttoken"));
	}
	
	@Test
	public void testCreateCustomerChannelTimeoutMin() {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
		when(channelService.createChannel(anyString(), anyInt())).thenReturn("newclienttoken");
		
		//#2 Request token with valid uid ...
		Optional<Integer> timeout = Optional.fromNullable(1);
		String result = ctr.createCustomerChannel(checkIn, timeout);
		verify(channelService).createChannel(anyString(), eq(timeout.get()));
		
		assertThat(result, is("newclienttoken"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateCustomerChannelTimeoutZero() {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
				
		//#2 Request token with valid uid ...
		ctr.createCustomerChannel(checkIn, Optional.fromNullable(0));
	}
	
	
	@Test
	public void testCreateCustomerChannel() {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
		when(channelService.createChannel(anyString())).thenReturn("newclienttoken");
				
		//#2 Request token with valid uid ...
		
		String result = ctr.createCustomerChannel(checkIn);
		verify(channelService).createChannel(anyString());
		assertThat(result, is("newclienttoken"));
	}
	
	@Test
	public void testCreateCustomerChannelRepeat() {
		CheckIn checkIn = mock(CheckIn.class);
		when(checkIn.getId()).thenReturn(1l);
		when(channelService.createChannel(anyString())).thenReturn("newclienttoken", "clienttoken2");
		
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn);
		assertThat(result, is("newclienttoken"));
			
		//#3.2 Request another token with the same uid, should create a new token ...
		String newResult = ctr.createCustomerChannel(checkIn);
		assertThat(newResult, is("clienttoken2"));
	}
	
	@Test(expected= NullPointerException.class)
	public void testSubscribeToBusinessNullClientId()  {
		ctr.subscribeToBusiness(null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testSubscribeToBusinessEmptyClientId()  {
		ctr.subscribeToBusiness("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSubscribeToBusinessInvalidClientId()  {
		ctr.subscribeToBusiness("b123d|21312123123123");
	}
	
	@Test
	public void testSubscribeToBusinessValidButUnknownClientId()  {
		ctr.subscribeToBusiness("b123|abcd");
	}
	
	@Test
	public void testSubscribeToBusiness() throws Exception {
		long businessId = 1;
		Business business = mock(Business.class);
		List<String> channelList = new ArrayList<String>();
		when(business.getChannelIds()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);
				
		String clientId = ctr.buildCockpitClientId(businessId, "test");
		ctr.subscribeToBusiness(clientId);
		
		verify(rr).saveOrUpdate(business);
	}
	
	@Test(expected=NullPointerException.class)
	public void testUnsubscribeFromBusinessBusinessNullClientId()  {
		ctr.unsubscribeFromBusiness(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnsubscribeFromBusinessBusinessEmptyClientId()  {
		ctr.unsubscribeFromBusiness("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnsubscribeFromBusinessBusinessInvalidClientId()  {
		ctr.unsubscribeFromBusiness("b123d|21312123123123");
	}
	
	@Test
	public void testUnsubscribeFromBusinessValidButUnknownClientId() throws Exception {
		long businessId = 1;
		Business business = mock(Business.class);
		List<String> channelList = new ArrayList<String>();
		channelList.add("clientid1");
		when(business.getChannelIds()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);

		ctr.unsubscribeFromBusiness(ctr.buildCockpitClientId(businessId, "unknown"));
		verify(rr, never()).saveOrUpdate(business);
		assertThat(channelList.size(), is(1));
	}
	
	@Test
	public void testUnsubscribeFromBusiness() throws Exception {
		long businessId = 1;
		Business business = mock(Business.class);
		List<String> channelList = new ArrayList<String>();
		String clientId = ctr.buildCockpitClientId(businessId, "test");
		channelList.add(clientId);
		when(business.getChannelIds()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);
		
		ctr.unsubscribeFromBusiness(clientId);
		verify(rr).saveOrUpdate(business);
		assertThat(channelList, not(hasItem(clientId)));
	}
	
	@Test(expected = NullPointerException.class)
	public void testUnsubscribeCheckInNullClientId() throws Exception {
		ctr.unsubscribeCheckIn(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testUnsubscribeCheckInEmptyClientId() throws Exception {
		ctr.unsubscribeCheckIn("");
	}

	@Test
	public void testUnsubscribeCheckInNewClientId() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1;
		String clientId = ctr.buildCustomerClientId(checkInId);
		
		when(cr.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getChannelId()).thenReturn(clientId);
		
		ctr.unsubscribeCheckIn(ctr.buildCustomerClientId(checkInId)+1);
		
		verify(checkIn, never()).setChannelId(null);
		verify(cr, never()).saveOrUpdate(checkIn);
	}
	
	@Test
	public void testUnsubscribeCheckInValidButUnknownClientId() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1;
		String clientId = ctr.buildCustomerClientId(checkInId);
		
		when(cr.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getChannelId()).thenReturn(clientId);
		
		ctr.unsubscribeCheckIn(ctr.buildCustomerClientId(1234));
		
		verify(checkIn, never()).setChannelId(null);
		verify(cr, never()).saveOrUpdate(checkIn);
	}
	
	@Test
	public void testUnsubscribeCheckIn() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1;
		String clientId = ctr.buildCustomerClientId(checkInId);
		
		when(cr.getById(checkInId)).thenReturn(checkIn);
		when(checkIn.getChannelId()).thenReturn(clientId);
		
		ctr.unsubscribeCheckIn(clientId);
		
		verify(checkIn).setChannelId(null);
		verify(cr).saveOrUpdate(checkIn);
	}
	
	@Test(expected=NullPointerException.class)
	public void testSubscribeCheckInNullClientId() throws Exception {
		ctr.subscribeCheckIn(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSubscribeCheckInEmptyClientId() throws Exception {
		ctr.subscribeCheckIn("");
	}
	
	@Test
	public void testSubscribeCheckInValidButUnknownClientId() throws Exception {
		ctr.subscribeCheckIn(ctr.buildCustomerClientId(2213));
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testSubscribeCheckInInvalidClientId() throws Exception {
		ctr.subscribeCheckIn("c12xxx|1234");
	}
	
	@Test
	public void testSubscribeCheckInRepeated() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1;
		when(cr.getById(checkInId)).thenReturn(checkIn);
		String clientId = ctr.buildCustomerClientId(checkInId);
		when(checkIn.getChannelId()).thenReturn(clientId);
		
		ctr.subscribeCheckIn(clientId);
		
		verify(cr, never()).saveOrUpdate(checkIn);
	}
	
	@Test
	public void testSubscribeCheckIn() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1;
		
		when(cr.getById(checkInId)).thenReturn(checkIn);
		
		String clientId = ctr.buildCustomerClientId( checkInId );
		
		ctr.subscribeCheckIn(clientId);
		
		verify(checkIn).setChannelId(clientId);
		verify(cr).saveOrUpdate(checkIn);
	}
	
	@Test
	public void testSendMessages() throws Exception {
		business = mock(Business.class);
		
		ArrayList<String> channelList = new ArrayList<String>();
		when(business.getChannelIds()).thenReturn(channelList);
		
		String clientId1 = "testclient1";
		channelList.add(clientId1);
		String clientId2 = "testclient2";
		channelList.add(clientId2);
		
		List<MessageDTO> content = new ArrayList<MessageDTO>();
		content.add(new MessageDTO("testtype","testaction", "content"));
		
		ctr.sendMessages(business, content );
		
		verify(channelService, times(2)).sendMessage(any(ChannelMessage.class));
	}
}
