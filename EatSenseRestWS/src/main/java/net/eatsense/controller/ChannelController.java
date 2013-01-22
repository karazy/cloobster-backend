package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.Channel;
import net.eatsense.event.ChannelOnlineCheckTimeOutEvent;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.ObjectifyKeyFactory;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.cockpit.MessageDTO;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Sends push messages via the Google Channel Service.
 * 
 * @author Nils Weiher
 *
 */
public class ChannelController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	LocationRepository businessRepo;
	
	ChannelService channelService;
	private ObjectMapper mapper;

	private CheckInRepository checkInRepo;

	private final ObjectifyKeyFactory ofyKeys;

	private Objectify ofy;

	private final Provider<net.eatsense.domain.Channel> channelPrv;

	private final EventBus eventBus;
	
	@Inject
	public ChannelController(LocationRepository rr, CheckInRepository checkInRepo, ObjectMapper mapper, ChannelService channelService,OfyService ofyService, Provider<net.eatsense.domain.Channel> channelPrv, EventBus eventBus) {
		super();
		this.businessRepo = rr;
		this.checkInRepo = checkInRepo;
		this.mapper = mapper;
		this.channelService = channelService;
		this.channelPrv = channelPrv;
		this.eventBus = eventBus;
		this.ofyKeys = ofyService.keys();
		this.ofy = ofyService.ofy();
	}
	
	/**
	 * Create a new message channel for push notification, with a default timeout of 2h,
	 * and register the client id in the business.
	 * 
	 * @param business datastore entity
	 * @param clientId
	 * @return the token to send to the client
	 */
	public String createCockpitChannel(Business business,Account account, String clientId) throws ChannelFailureException {
		return createCockpitChannel(business, account,  clientId, Optional.<Integer>absent());
	}
	
	/**
	 * Create a new message channel for business administration push notification, with the given timeout.
	 * 
	 * @param business datastore entity
	 * @param clientId
	 * @param timeout in minutes
	 * @return the token to send to the client
	 * @throws ChannelFailureException if channel creation failed
	 */
	public String createCockpitChannel(Business business, Account account, String clientId , Optional<Integer> timeout) throws ChannelFailureException {
		checkNotNull(business, "business was null");
		checkNotNull(business.getId(), "business id was null");
		checkNotNull(account, "account was null");
		checkNotNull(clientId, "clientId was null");
		checkArgument(!clientId.isEmpty(), "clientId was empty");
		checkArgument(!timeout.isPresent() || ( timeout.get() > 0 && timeout.get() < 24*60),"timeout expected > 0 and < 24*60 minutes, but was %s",timeout.orNull());
		
		String token = null;
		// create a channel id with the format "businessId|clientId"
		
		int lastIndexOf = clientId.lastIndexOf("-");
		String pureClientId;
		if(lastIndexOf == -1) {
			pureClientId = clientId;
		}
		else {
			pureClientId = clientId.substring(0 , lastIndexOf);
		}
		
		net.eatsense.domain.Channel channel = channelPrv.get();
		channel.setAccount(account.getKey());
		channel.setBusiness(business.getKey());
		channel.setClientId(pureClientId);
		channel.setCreationTime(new Date());
		ofy.put(channel);
		
		clientId = buildCockpitClientId(business.getId(), clientId);
		
		logger.debug("clientId: {}, timeout: {}", clientId, timeout);
		token = (timeout.isPresent())?channelService.createChannel(clientId, timeout.get()):channelService.createChannel(clientId);

		return token;
	}
	
	/**
	 * Send message to open channel of the checkin.
	 * 
	 * @param checkInId
	 * @param messageData
	 * @return message as string
	 * @throws IllegalArgumentException if unknown checkInId has been passed
	 */
	public String sendMessageToCheckIn(long checkInId, MessageDTO messageData) throws IllegalArgumentException {
		checkArgument(checkInId != 0, "checkInId cannot be 0");
		checkNotNull(messageData, "messageData cannot be null");
		
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Failed to send message, unknow checkInId", e);
		}
		if(checkIn.getChannelId() != null)
			return sendMessage(checkIn.getChannelId(), messageData);
		else {
			String buildJsonMessage = buildJsonMessage(messageData);
			logger.info("no channel connected for checkin {}\nSkipping message: {}", checkInId, buildJsonMessage);
			return buildJsonMessage;
		}
	}
	
	/**
	 * Send a message to the client identified by the client id.
	 * 
	 * @param clientId
	 * @param type
	 * @param action 
	 * @param content an object for data serialization to JSON
	 * @return the complete message string 
	 * 
	 */
	public String sendMessage(String clientId, MessageDTO messageData) throws IllegalArgumentException {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		checkNotNull(messageData, "messageData cannot be null");
		checkNotNull(messageData.getType(), "message type cannot be null");
		checkNotNull(messageData.getAction(), "message action cannot be null");
		checkArgument(!messageData.getType().isEmpty(), "message type cannot be empty");
		checkArgument(!messageData.getAction().isEmpty(), "message action cannot be empty");
		
		return sendMessageRaw(clientId, buildJsonMessage(messageData)).getMessage();
	}
	

	/**
	 * Build a JSON string from the given POJO. 
	 * 
	 * @param object
	 * @return JSON string representing the object
	 */
	private String buildJsonMessage(Object object) {
		checkNotNull(object, "object cannot be null");
		String messageString;
		
		try {
			// Try to map the message data as a JSON string.
			messageString = mapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot build message, error mapping object to JSON : "+ object, e);
		}
		
		return messageString;
	}
	
	
	/**
	 * Send message with the given string content.
	 *  
	 * @param clientId identifying the client(channel) to send to
	 * @param messageString the data to send with the message, limited to 32 kb as UTF-8 string
	 * @return
	 */
	private ChannelMessage sendMessageRaw(String clientId, String messageString) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		checkNotNull(messageString, "messageString cannot be null");
		checkArgument(!messageString.isEmpty(), "messageString cannot be empty");
		
		if(messageString.getBytes().length > 32786)
			throw new IllegalArgumentException("Cannot send message, data package longer than 32kB");

		ChannelMessage message = new ChannelMessage(clientId, messageString);
		logger.info("Sending to client {}, message: {}", clientId, message.getMessage());
		channelService.sendMessage(message);
		
		return message;
	}
	
	/**
	 * Send a list of messages as one package to all subscribed channels of this business as a JSON array.
	 * 
	 * @param businessId
	 * @param messages
	 */
	public void sendMessage(Business business, MessageDTO messageData)  {
		checkNotNull(business, "business cannot be null");
		checkNotNull(messageData, "messageData cannot be null");
		checkNotNull(messageData.getType(), "message type cannot be null");
		checkNotNull(messageData.getAction(), "message action cannot be null");
		checkArgument(!messageData.getType().isEmpty(), "message type cannot be empty");
		checkArgument(!messageData.getAction().isEmpty(), "message action cannot be empty");

		sendJsonObject(business, messageData);
	}
	
	/**
	 * Send a list of messages as one package to all subscribed channels of this business as a JSON array.
	 * 
	 * @param businessId
	 * @param messages
	 */
	public void sendMessages(Business business, List<MessageDTO> content)  {
		checkNotNull(business, "business cannot be null");
		if(content != null && !content.isEmpty())
			sendJsonObject(business, content);
		else
			logger.info("nothing to send, content null or empty");
	}
	
	private void sendJsonObject(Business business, Object content)  {
		checkNotNull(business, "business cannot be null");
		checkNotNull(content, "content cannot be null");
		
		String messageString = buildJsonMessage(content);
		
		if(!business.getChannels().isEmpty()) {
			// Send to all clients registered to the business ...
			for (Channel client : business.getChannels()) {
				sendMessageRaw(client.getClientId(), messageString);
				logger.debug("sent message to channel {} ", client);
			}
		}
		else {
			logger.info("no open channels to send message: {}", messageString);
		}
			
	}

	/**
	 * Called by the Channel API, after a channel was disconnected.<br>
	 * Unsubscribe this channel, from existing message subscriptions.
	 * 
	 * @param request
	 */
	public void handleDisconnected(HttpServletRequest request) {
		String clientId;
		try {
			clientId = channelService.parsePresence(request).clientId();
		} catch (IOException e) {
			throw new RuntimeException("could not parse presence",e);
		}
		logger.debug("recieved disconnected from clientId:" + clientId);
		if(clientId.startsWith("c")) {
			unsubscribeCheckIn(clientId);
		}
		else if(clientId.startsWith("b"))
			unsubscribeFromBusiness(clientId);
	}
	
	
	/**
	 * Called by the Channel API, after a channel was connected.<br>
	 * Subscribe this channel for updates of this business.
	 * 
	 * @param request
	 * @throws IOException 
	 */
	public void handleConnected(HttpServletRequest request) {
		String clientId;

		try {
			clientId = channelService.parsePresence(request).clientId();
		} catch (IOException e) {
			logger.error("could not parse presence",e);
			return;
		}
		logger.debug("recieved connected from clientId:" + clientId);
		if(clientId.startsWith("c")) {
			subscribeCheckIn(clientId);
		}
		else if(clientId.startsWith("b"))
			subscribeToBusiness(clientId);
		
		sendMessage(clientId, new MessageDTO("channel","connected", null));
	}

	/**
	 * @param clientId
	 */
	public String checkOnlineStatus(long businessId, String clientId) {
		boolean connected = false;
		logger.info("recieved online check from clientId:" + clientId);

		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (NotFoundException e) {
			logger.warn("Unknown businessId");
			business = null;
		}
		int lastIndexOf = clientId.lastIndexOf("-");
		
		String pureClientId;
		if(lastIndexOf == -1) {
			pureClientId = clientId;
		}
		else {
			pureClientId = clientId.substring(0, lastIndexOf);
		}
		
		net.eatsense.domain.Channel channelEntity = ofy.find(ofyKeys.create(business.getKey(), net.eatsense.domain.Channel.class, pureClientId));
		if(channelEntity == null) {
			logger.warn("Unable to track channel status, can't find Channel logging entity for clientId: {}", pureClientId);
		}
		else {
			channelEntity.setLastOnlineCheck(new Date());
			ofy.async().put(channelEntity);
		}
		
		clientId = buildCockpitClientId(businessId, clientId);
		
		connected = (business != null && business.getChannels().contains( Channel.fromClientId(clientId)));

		if(connected) {
			sendMessage(clientId, new MessageDTO("channel","connected", null));
			return "CONNECTED";
		}
		else {
			return "DISCONNECTED";
		}
	}
	
	public String checkOnlineStatusOfCheckIn(String checkInUid) {
		boolean connected = false;
		logger.debug("recieved online check from clientId:" + checkInUid);
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInUid);

		connected = (checkIn != null && checkIn.getChannelId() != null);
		
		if(connected) {
			sendMessage(checkIn.getChannelId(), new MessageDTO("channel","connected", null));
			return "CONNECTED";
		}
		else {
			return "DISCONNECTED";
		}
	}
	
	/**
	 * @param clientId
	 */
	public void subscribeCheckIn(String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		
		CheckIn checkIn = parseCheckIn(clientId);
		if(checkIn == null)
			return;
		
		if (!clientId.equals(checkIn.getChannelId())) {
			checkIn.setChannelId(clientId);
			logger.info("Subscribing channel {} to checkin {} ", clientId, checkIn.getNickname());
			checkInRepo.saveOrUpdate(checkIn);
		}
	}
	
	public void unsubscribeCheckIn(String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		
		CheckIn checkIn = parseCheckIn(clientId);
		if(checkIn == null)
			return;
		
		if(checkIn.getChannelId() != null && checkIn.getChannelId().equals(clientId)) {
			checkIn.setChannelId(null);
			logger.info("Channel disconnected for CheckIn.id={}, nickname={}", checkIn.getId(), checkIn.getNickname());
			
			// Disabled updating of checking because of problematic caching
			// and datastore deletes leading to inconsitencies after checkin deletion.
			//checkInRepo.saveOrUpdate(checkIn);
		}
	}

	/**
	 * Remove the channel from the business<br>
	 * It doesnt recieve further messages, concerning this business.
	 * 
	 * @param clientId
	 */
	public void unsubscribeFromBusiness(String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");

		Business business = parseBusiness(clientId);
		if(business == null) {
			return;
		}
		else {
			removeChannelFromBusiness(clientId, business);
		}
	}

	/**
	 * @param clientId
	 * @param business
	 */
	private void removeChannelFromBusiness(String clientId, Business business) {
		if (business.getChannels().isEmpty())	{
			return;
		}
		Channel channel = Channel.fromClientId(clientId);
		if(business.getChannels().contains(channel)) {
			business.getChannels().remove(channel);
			logger.info("Unsubscribing channel {} from business {} ", clientId, business.getName());
			businessRepo.saveOrUpdate(business);
		}
	}
	
	/**
	 * Get the Business entity by the id saved in the given clientId string.
	 * 
	 * @param clientId
	 * @return Business entity
	 */
	private Business parseBusiness(String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		checkArgument(clientId.matches("b[1-9]\\d*\\|.*"), "invalid clientId %s", clientId);
		
		// retrieve the businessId appended at the front of the client id seperated with a "|"
		Long businessId = Long.valueOf(clientId.substring(1, clientId.indexOf("|") ));

		try {
			return businessRepo.getById(businessId);
		} catch (NotFoundException e) {
			logger.error("clientId contains unknown encoded business, clientId={}",clientId, e);
			return null;
		}
	}
	
	/**
	 * Get the CheckIn entity by the encoded id.
	 * 
	 * @param clientId
	 * @return CheckIn entity
	 */
	private CheckIn parseCheckIn(String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		checkArgument(clientId.matches("c[1-9]\\d*\\|\\d*"), "invalid clientId %s", clientId);
		
		// retrieve the id encoded after the first character in the string
		Long checkInId = Long.valueOf(clientId.substring(1, clientId.indexOf("|")));
		try {
			return checkInRepo.getById(checkInId);
		} catch (NotFoundException e) {
			logger.error("clientId contains unknown encoded checkIn, clientId={}", clientId);
			return null;
		}
	}
	
	/**
	 * Save the given client as a listener to updates for this business.
	 * 
	 * @param clientId
	 */
	public void subscribeToBusiness( String clientId) {
		checkNotNull(clientId, "clientId cannot be null");
		checkArgument(!clientId.isEmpty(), "clientId cannot be empty");
		
		Business business = parseBusiness(clientId);
		if(business == null)
			return;
		
		Calendar calendar = Calendar.getInstance();
		Integer timeout;
		try {
			timeout = Integer.valueOf(System.getProperty("net.karazy.channels.cockpit.timeout"));
		} catch (NumberFormatException e) {
			timeout = 120;
		}
		calendar.add(Calendar.MINUTE, -timeout);
		
		Channel newChannel = Channel.fromClientId(clientId);
		
		int lastIndexOf = clientId.lastIndexOf("-");
		String pureClientId;
		if(lastIndexOf == -1) {
			pureClientId = clientId.substring(clientId.lastIndexOf("|")+1);
		}
		else {
			pureClientId = clientId.substring(clientId.lastIndexOf("|")+1 , lastIndexOf);
		}
		
		net.eatsense.domain.Channel channelEntity = ofy.find(ofyKeys.create(business.getKey(), net.eatsense.domain.Channel.class, pureClientId));
		if(channelEntity == null) {
			logger.warn("Unable to track channel status, can't find Channel logging entity for clientId: {}", pureClientId);
		}
		else {
			channelEntity.setLastChannelId(clientId);
			channelEntity.setChannelCount(channelEntity.getChannelCount()+1);
			channelEntity.setLastOnlineCheck(new Date());
			ofy.async().put(channelEntity);
		}
		
		for (Iterator<Channel> iterator = business.getChannels().iterator(); iterator.hasNext();) {
			Channel channel  = iterator.next();
			if(channel.getCreationDate().before(calendar.getTime())) {
				iterator.remove();
			}
		}
		
		if(!business.getChannels().contains(newChannel)) {
			business.getChannels().add(newChannel);
			logger.info("Subscribing channel {} to business {} ", clientId, business.getName());
		}
		businessRepo.saveOrUpdate(business);
	}
	
	/**
	 * Create a new message channel for customer push notification, with the given timeout.
	 * 
	 * @param checkInUid uid of the connecting checkin
	 * @param timeout in minutes
	 * @return the token to send to the client
	 */
	public String createCustomerChannel(CheckIn checkIn, Optional<Integer> timeout) {
		checkNotNull(checkIn, "checkIn cannot be null");
		checkNotNull(checkIn.getId(), "checkIn id cannot be null");
		checkArgument(!timeout.isPresent() || ( timeout.get() > 0 && timeout.get() < 24*60),"timeout expected > 0 and < 24*60 minutes, but was %s",timeout.orNull());
		
		// create a channel id with the format "c{checkInId}"
		String clientId = buildCustomerClientId(checkIn.getId());
		
		logger.debug("clientId: {}, timeout: {}", clientId, timeout);
		// request a new token, with or without timeout depending if set
		return (timeout.isPresent())?channelService.createChannel(clientId, timeout.get()):channelService.createChannel(clientId);
	}

	/**
	 * @param checkIn
	 * @return
	 */
	public String buildCustomerClientId(long checkInId) {
		checkArgument(checkInId != 0 , "checkInId expected != 0");
		return "c"+ String.valueOf(checkInId) + "|" + String.valueOf(new Date().getTime());
	}
	
	/**
	 * Generates and returns a new channel token.
	 * 
	 * @param checkIn entity which initiated the request
	 * @param clientId to use for token creation 
	 * @return the generated channel token
	 */
	public String createCustomerChannel(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn cannot be null");
		
		return createCustomerChannel(checkIn, Optional.<Integer>absent());
	}

	public String buildCockpitClientId(long businessId, String clientId) {
		checkArgument(businessId != 0 , "businessId was 0");
		checkNotNull(clientId, "clientId was null");
		return "b"+ String.valueOf(businessId) + "|" + String.valueOf(clientId);
	}
	
	/**
	 * Handle called by the cockpit after a manual logout
	 * 
	 * @param business
	 * @param clientId
	 */
	public void disconnectCockpitChannel(Business business, String clientId ) {
		checkNotNull(business, "business was null");
		checkNotNull(clientId, "clientId was null");
		
		logger.info("Logout received from clientId={}, removing channel.", clientId);
		int lastIndexOf = clientId.lastIndexOf("-");
		
		String pureClientId;
		if(lastIndexOf == -1) {
			pureClientId = clientId;
		}
		else {
			pureClientId = clientId.substring(0, lastIndexOf);
		}
		clientId = buildCockpitClientId(business.getId(), clientId);
		removeChannelFromBusiness(clientId, business);
		
		// Remove tracking entity from datastore
		ofy.delete(ofyKeys.create(business.getKey(), net.eatsense.domain.Channel.class, pureClientId));
	}
	
	/**
	 * Iterate over all channel tracking entities and check the time of the last online ping.
	 * Issue an e-mail alert if the online check was longer than a set amout of minutes ago.
	 */
	public void checkAllOnlineChannels() {
		QueryResultIterable<net.eatsense.domain.Channel> channelQueryResult = ofy.query(net.eatsense.domain.Channel.class).fetch();
		logger.info("Checking all availabe cockpit channels ...");
		// Time in minutes the last online check of an active channel should be ago.
		int onlineCheckTimeout = 30;
		
		Calendar calendar = Calendar.getInstance();
		try {
			onlineCheckTimeout = Integer.valueOf(System.getProperty("net.karazy.channels.cockpit.offlinewarning"));
		} catch (NumberFormatException e) {
			logger.warn("net.karazy.channels.cockpit.offlinewarning property not set correctly.");
		}

		calendar.add(Calendar.MINUTE, -onlineCheckTimeout);
		int channelsActive = 0;
		for (net.eatsense.domain.Channel channel : channelQueryResult) {
			channelsActive++;
			if(channel.getLastOnlineCheck().before(calendar.getTime())) {
				logger.warn("Channel={} had no online check for {} minutes", channel.getLastChannelId(), onlineCheckTimeout);
				if(!channel.isWarningSent()) {
					eventBus.post(new ChannelOnlineCheckTimeOutEvent(channel));
					channel.setWarningSent(true);
					ofy.async().put(channel);
				}
				else {
					logger.warn("E-mail alert already sent.");
				}
			}
		}
		logger.info("{} cockpit client should be active.", channelsActive);
	}
}
