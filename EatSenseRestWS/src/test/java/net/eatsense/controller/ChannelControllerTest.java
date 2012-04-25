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

import java.util.ArrayList;
import java.util.List;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
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

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.tools.development.testing.LocalChannelServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class ChannelControllerTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig() , new LocalChannelServiceTestConfig());
    
    private Injector injector;
    private ChannelController ctr;
    private BusinessRepository rr;
    private SpotRepository br;
    private CheckInRepository cr;

	private ObjectMapper mapper;

	private Business business;

	private CheckInController checkInCtrl;

	private ChannelService channelService;
	

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		channelService = ChannelServiceFactory.getChannelService();
		injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		ctr = injector.getInstance(ChannelController.class);
		checkInCtrl = injector.getInstance(CheckInController.class);
		rr = injector.getInstance(BusinessRepository.class);
		br = injector.getInstance(SpotRepository.class);
		cr = injector.getInstance(CheckInRepository.class);
		mapper = injector.getInstance(ObjectMapper.class);
		//create necessary data in datastore
		business = new Business();
		business.setName("Heidi und Paul");
		business.setDescription("Geiles Bio Burger Restaurant.");
		Key<Business> kR = rr.saveOrUpdate(business);
		
		Spot b = new Spot();
		b.setBarcode("b4rc0de");
		b.setName("Tisch 1");
		b.setBusiness(kR);
		Key<Spot> kB = br.saveOrUpdate(b); 
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
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
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn, Optional.fromNullable(24*60 +1));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test
	public void testCreateCustomerChannelTimeoutMax() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn, Optional.fromNullable(24*60-1));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test
	public void testCreateCustomerChannelTimeoutMin() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn, Optional.fromNullable(1));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateCustomerChannelTimeoutZero() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn, Optional.fromNullable(0));
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	
	@Test
	public void testCreateCustomerChannel() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn);
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test
	public void testCreateCustomerChannelRepeat() {
		//#1 Create a checkin ...
		CheckInDTO checkInData = new CheckInDTO();
		checkInData.setSpotId("b4rc0de");
		checkInData.setNickname("FakeNik");
		checkInData.setStatus(CheckInStatus.INTENT);
		checkInData = checkInCtrl.createCheckIn( checkInData);
		
		CheckIn checkIn = checkInCtrl.getCheckIn(checkInData.getUserId());
				
		//#2 Request token with valid uid ...
		String result = ctr.createCustomerChannel(checkIn);
		assertThat(result, notNullValue());
		assertThat(result.isEmpty(), is(false));
			
		//#3.2 Request another token with the same uid, should create a new token ...
		String newResult = ctr.createCustomerChannel(checkIn);
		assertThat(newResult, is(not(result)));
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
		String clientId = ctr.buildCockpitClientId(business.getId(), "test");
		ctr.subscribeToBusiness(clientId);
		Business businessTest = rr.getByKey(business.getKey());
		assertThat(businessTest.getChannelIds(), hasItem(clientId));
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
		String clientId = ctr.buildCockpitClientId(business.getId(), "test");
		ctr.subscribeToBusiness(clientId);
		ctr.unsubscribeFromBusiness(ctr.buildCockpitClientId(business.getId(), "unknown"));
		Business businessTest = rr.getByKey(business.getKey());
		assertThat(businessTest.getChannelIds(), hasItem(clientId));
	}
	
	@Test
	public void testUnsubscribeFromBusiness() throws Exception {
		String clientId = ctr.buildCockpitClientId(business.getId(), "test");
		ctr.subscribeToBusiness(clientId);
		ctr.unsubscribeFromBusiness(clientId);
		
		Business businessTest = rr.getByKey(business.getKey());
		assertThat(businessTest.getChannelIds(), not(hasItem(clientId)));
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
		CheckIn checkIn = new CheckIn();
		cr.saveOrUpdate(checkIn);
		
		String clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		ctr.unsubscribeCheckIn(ctr.buildCustomerClientId(checkIn.getId()));
		
		checkIn = cr.getByKey(checkIn.getKey());
		assertThat(checkIn.getChannelId(), is(clientId));
	}
	
	@Test
	public void testUnsubscribeCheckInValidButUnknownClientId() throws Exception {
		CheckIn checkIn = new CheckIn();
		cr.saveOrUpdate(checkIn);
		
		String clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		ctr.unsubscribeCheckIn(ctr.buildCustomerClientId(1234));
		
		checkIn = cr.getByKey(checkIn.getKey());
		assertThat(checkIn.getChannelId(), is(clientId));
	}
	
	@Test
	public void testUnsubscribeCheckIn() throws Exception {
		CheckIn checkIn = new CheckIn();
		cr.saveOrUpdate(checkIn);
		
		String clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		ctr.unsubscribeCheckIn(clientId);
		
		checkIn = cr.getByKey(checkIn.getKey());
		assertThat(checkIn.getChannelId(), nullValue());
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
		CheckIn checkIn = new CheckIn();
		cr.saveOrUpdate(checkIn);
		
		String clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		
		checkIn = cr.getByKey(checkIn.getKey());
		assertThat(checkIn.getChannelId(), is(clientId));
	}
	
	@Test
	public void testSubscribeCheckIn() throws Exception {
		CheckIn checkIn = new CheckIn();
		cr.saveOrUpdate(checkIn);
		
		String clientId = ctr.buildCustomerClientId(checkIn.getId());
		ctr.subscribeCheckIn(clientId);
		
		checkIn = cr.getByKey(checkIn.getKey());
		assertThat(checkIn.getChannelId(), is(clientId));
	}
	
	@Test
	public void testSendMessages() throws Exception {
		business.setChannelIds(new ArrayList<String>());
		String clientId1 = "testclient1";
		channelService.createChannel(clientId1);
		business.getChannelIds().add(clientId1);
		String clientId2 = "testclient2";
		channelService.createChannel(clientId2);
		business.getChannelIds().add(clientId2);
		
		List<MessageDTO> content = new ArrayList<MessageDTO>();
		content.add(new MessageDTO("testtype","testaction", "content"));
		ctr.sendMessages(business, content );
	}
}
