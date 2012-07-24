package net.eatsense.controller;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.embedded.Channel;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.representation.cockpit.MessageDTO;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.base.Optional;
import com.googlecode.objectify.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class ChannelControllerTest {
	
    private ChannelController ctr;
    @Mock
    private BusinessRepository rr;
    @Mock
    private CheckInRepository cr;
    
    @Mock
    private ChannelService channelService;
    @Mock
	private Business business;
	@Mock
	private ObjectMapper jsonMapper;

	@Before
	public void setUp() throws Exception {
		ctr = new ChannelController(rr, cr, jsonMapper, channelService);
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
		Set<Channel> channelList = new HashSet<Channel>();
		when(business.getChannels()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);
				
		String clientId = ctr.buildCockpitClientId(businessId, "test");
		ctr.subscribeToBusiness(clientId);
				
		verify(rr).saveOrUpdate(business);
		assertThat(channelList.contains(Channel.fromClientId(clientId)), is (true));
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
		Set<Channel> channelList = new HashSet<Channel>();
		channelList.add(Channel.fromClientId("clientid1"));
		when(business.getChannels()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);

		ctr.unsubscribeFromBusiness(ctr.buildCockpitClientId(businessId, "unknown"));
		verify(rr, never()).saveOrUpdate(business);
		assertThat(channelList.size(), is(1));
	}
	
	@Test
	public void testUnsubscribeFromBusiness() throws Exception {
		long businessId = 1;
		Business business = mock(Business.class);
		Set<Channel> channelList = new HashSet<Channel>();
		String clientId = ctr.buildCockpitClientId(businessId, "test");
		Channel channel = Channel.fromClientId(clientId);
		channelList.add(channel);
		when(business.getChannels()).thenReturn(channelList );
		when(rr.getById(businessId)).thenReturn(business);
		
		ctr.unsubscribeFromBusiness(clientId);
		verify(rr).saveOrUpdate(business);
		assertThat(channelList, not(hasItem(channel)));
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
	public void testSendMessagesEmptyContent() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessages(business, new ArrayList<MessageDTO>());
		
		verify(channelService, never()).sendMessage(any(ChannelMessage.class));
	}
	
	@Test
	public void testSendMessagesNullContent() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessages(business, null);
		
		verify(channelService, never()).sendMessage(any(ChannelMessage.class));
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendMessagesNullBusiness() throws Exception {
		List<MessageDTO> content = new ArrayList<MessageDTO>();
		ctr.sendMessages(null, content);
	}
	
	@Test
	public void testSendMessages() throws Exception {
		business = mock(Business.class);
		
		Set<Channel> channelList = new HashSet<Channel>();
		when(business.getChannels()).thenReturn(channelList);
		
		String clientId1 = "testclient1";
		channelList.add(Channel.fromClientId(clientId1));
		String clientId2 = "testclient2";
		channelList.add(Channel.fromClientId(clientId2));
		
		List<MessageDTO> content = new ArrayList<MessageDTO>();
		content.add(new MessageDTO("testtype","testaction", "content"));
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessages(business, content );
		ArgumentCaptor<ChannelMessage> messageArgument = ArgumentCaptor.forClass(ChannelMessage.class);

		verify(channelService, times(2)).sendMessage(messageArgument.capture());
		List<ChannelMessage> messages = messageArgument.getAllValues();
		assertThat(messages.get(0).getClientId(), anyOf(is(clientId1), is(clientId2)));
		assertThat(messages.get(1).getClientId(), anyOf(is(clientId1), is(clientId2)));
		
		assertThat(messages.get(0).getMessage(), is(messageString));
		assertThat(messages.get(1).getMessage(), is(messageString));
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendMessageNullContent() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendMessageNullBusiness() throws Exception {
		business = null;
		
		ctr.sendMessage(business, new MessageDTO("testtype","testaction", "content"));
	}
	
	@Test
	public void testSendMessageNullMessageContent() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, new MessageDTO("type","action", null));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSendMessageEmptyAction() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, new MessageDTO("type","", null));
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendMessageNullAction() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, new MessageDTO("type",null, null));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSendMessageEmptyType() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, new MessageDTO("","testaction", null));
	}
	
	@Test(expected = NullPointerException.class)
	public void testSendMessageNullType() throws Exception {
		Business business = mock(Business.class);
		ctr.sendMessage(business, new MessageDTO(null,"testaction", null));
	}
	
	@Test
	public void testSendMessage() throws Exception {
		business = mock(Business.class);
		
		Set<Channel> channelList = new HashSet<Channel>();
		when(business.getChannels()).thenReturn(channelList);
		
		String clientId1 = "testclient1";
		channelList.add(Channel.fromClientId(clientId1));
		String clientId2 = "testclient2";
		channelList.add(Channel.fromClientId(clientId2));
		
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessage(business, content);
		ArgumentCaptor<ChannelMessage> messageArgument = ArgumentCaptor.forClass(ChannelMessage.class);

		verify(channelService, times(2)).sendMessage(messageArgument.capture());
		List<ChannelMessage> messages = messageArgument.getAllValues();
		assertThat(messages.get(0).getClientId(), anyOf(is(clientId1), is(clientId2)));
		assertThat(messages.get(1).getClientId(), anyOf(is(clientId1), is(clientId2)));
		
		assertThat(messages.get(0).getMessage(), is(messageString));
		assertThat(messages.get(1).getMessage(), is(messageString));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected= IllegalArgumentException.class)
	public void testSendMessageClientIdJsonException() throws Exception {
		String clientId1 = "testclient1";
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		when(jsonMapper.writeValueAsString(content)).thenThrow(JsonMappingException.class);
		
		ctr.sendMessage(clientId1, content);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageClientIdMessageTooLong() throws Exception {
		String clientId1 = "testclient1";
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		when(jsonMapper.writeValueAsString(content)).thenReturn(com.google.common.base.Strings.repeat("12", 320000));
		
		ctr.sendMessage(clientId1, content);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageClientIdEmptyAction() throws Exception {
		String clientId1 = "testclient1";
		ctr.sendMessage(clientId1, new MessageDTO("type","", "content"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageClientIdEmptyType() throws Exception {
		String clientId1 = "testclient1";
		ctr.sendMessage(clientId1, new MessageDTO("","testaction", "content"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testSendMessageClientIdNullAction() throws Exception {
		String clientId1 = "testclient1";
		ctr.sendMessage(clientId1, new MessageDTO("type",null, "content"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testSendMessageClientIdNullType() throws Exception {
		String clientId1 = "testclient1";
		ctr.sendMessage(clientId1, new MessageDTO(null,"testaction", "content"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSendMessageEmptyClientId() throws Exception {
		String clientId1 = "";
		ctr.sendMessage(clientId1, new MessageDTO("testtype","testaction", "content"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testSendMessageNullClientId() throws Exception {
		String clientId1 = null;
		ctr.sendMessage(clientId1, new MessageDTO("testtype","testaction", "content"));
	}
	
	@Test
	public void testSendMessageClientId() throws Exception {
		String clientId1 = "testclient1";
		
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessage(clientId1, content);
		ArgumentCaptor<ChannelMessage> messageArgument = ArgumentCaptor.forClass(ChannelMessage.class);

		verify(channelService, times(1)).sendMessage(messageArgument.capture());
		List<ChannelMessage> messages = messageArgument.getAllValues();
		assertThat(messages.get(0).getClientId(), is(clientId1));
		assertThat(messages.get(0).getMessage(), is(messageString));
	}
	
	@Test(expected= NullPointerException.class)
	public void testSendMessageToCheckInNullMessage() throws Exception {
		long checkInId = 1l;
		
		ctr.sendMessageToCheckIn(checkInId, null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testSendMessageToCheckInZeroId() throws Exception {
		long checkInId = 0l;
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		ctr.sendMessageToCheckIn(checkInId, content);
	}
	
	@Test
	public void testSendMessageToCheckInNoChannel() throws Exception {
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1l;
		when(checkIn.getId()).thenReturn(checkInId);
		when(cr.getById(checkInId)).thenReturn(checkIn);
		
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessageToCheckIn(checkInId, content);
		verify(channelService, never()).sendMessage(any(ChannelMessage.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected= IllegalArgumentException.class)
	public void testSendMessageToCheckInUnknown() throws Exception {
		long checkInId = 1l;
		when(cr.getById(checkInId)).thenThrow(NotFoundException.class);
		
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessageToCheckIn(checkInId, content);
	}
	
	@Test
	public void testSendMessageToCheckIn() throws Exception {
		String clientId1 = "testclient1";
		CheckIn checkIn = mock(CheckIn.class);
		long checkInId = 1l;
		when(checkIn.getId()).thenReturn(checkInId);
		when(checkIn.getChannelId()).thenReturn(clientId1);
		when(cr.getById(checkInId)).thenReturn(checkIn);
		
		MessageDTO content = new MessageDTO("testtype","testaction", "content");
		
		String messageString = "jsonmessagestring";
		when(jsonMapper.writeValueAsString(content)).thenReturn(messageString);
		
		ctr.sendMessageToCheckIn(checkInId, content);
		ArgumentCaptor<ChannelMessage> messageArgument = ArgumentCaptor.forClass(ChannelMessage.class);

		verify(channelService, times(1)).sendMessage(messageArgument.capture());
		List<ChannelMessage> messages = messageArgument.getAllValues();
		assertThat(messages.get(0).getClientId(), is(clientId1));
		assertThat(messages.get(0).getMessage(), is(messageString));
	}
	
	@Test
	public void testCreateCockpitChannelWithTimeout() throws Exception {
		long businessId = 1;
		String clientId = "test";
		Optional<Integer> timeout = Optional.of(100);
		when(business.getId()).thenReturn(businessId);
		when(channelService.createChannel(ctr.buildCockpitClientId(businessId, clientId), timeout.get())).thenReturn("newclienttoken");
		
		//#2 Request token with valid uid ...
		
		String result = ctr.createCockpitChannel(business, clientId, timeout);
		assertThat(result, is("newclienttoken"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateCockpitChannelNullBusinessId() throws Exception {
		when(business.getId()).thenReturn(null);
		String clientId = "test";
		Optional<Integer> timeout = Optional.absent();
		
		ctr.createCockpitChannel(business, clientId, timeout);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateCockpitChannelEmptyClientId() throws Exception {
		long businessId = 1;
		when(business.getId()).thenReturn(businessId);
		String clientId = "";
		Optional<Integer> timeout = Optional.absent();
		
		ctr.createCockpitChannel(business, clientId, timeout);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateCockpitChannelNullClientId() throws Exception {
		long businessId = 1;
		when(business.getId()).thenReturn(businessId);
		String clientId = null;
		Optional<Integer> timeout = Optional.absent();
		
		ctr.createCockpitChannel(business, clientId, timeout);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateCockpitChannelNullBusiness() throws Exception {
		business = null;
		String clientId = "test";
		Optional<Integer> timeout = Optional.absent();
		
		ctr.createCockpitChannel(business, clientId, timeout);
	}
	
	@Test
	public void testCreateCockpitChannel() throws Exception {
		long businessId = 1;
		String clientId = "test";
		Optional<Integer> timeout = Optional.absent();
		when(business.getId()).thenReturn(businessId);
		when(channelService.createChannel(ctr.buildCockpitClientId(businessId, clientId))).thenReturn("newclienttoken");
		
		//#2 Request token with valid uid ...
		
		String result = ctr.createCockpitChannel(business, clientId, timeout);
		assertThat(result, is("newclienttoken"));
	}
}
