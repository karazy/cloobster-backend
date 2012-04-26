package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.representation.cockpit.MessageDTO;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

/**
 * Sends push messages via the Google Channel Service.
 * 
 * @author Nils Weiher
 *
 */
public class ChannelController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	BusinessRepository businessRepo;
	
	ChannelService channelService;
	private ObjectMapper mapper;

	private CheckInRepository checkInRepo;
	
	@Inject
	public ChannelController(BusinessRepository rr, CheckInRepository checkInRepo, ObjectMapper mapper, ChannelService channelService) {
		super();
		this.businessRepo = rr;
		this.checkInRepo = checkInRepo;
		this.mapper = mapper;
		this.channelService = channelService;
	}
	
	/**
	 * Create a new message channel for push notification.
	 * 
	 * @param businessId id of a business
	 * @param clientId
	 * @return
	 */
	public String createCockpitChannel(long businessId, String clientId) throws ChannelFailureException {
		return createCockpitChannel(businessId, clientId, Optional.<Integer>absent());
	}
	
	/**
	 * Create a new message channel for push notification, with a default timeout of 2h,
	 * and register the client id in the business.
	 * 
	 * @param business datastore entity
	 * @param clientId
	 * @return the token to send to the client
	 */
	public String createCockpitChannel(Business business, String clientId) throws ChannelFailureException {
		return createCockpitChannel(business, clientId, Optional.<Integer>absent());
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
	public String createCockpitChannel(Business business, String clientId , Optional<Integer> timeout) throws ChannelFailureException {
		checkNotNull(business, "business was null");
		checkNotNull(business.getId(), "business id was null");
		checkNotNull(clientId, "clientId was null");
		checkArgument(!clientId.isEmpty(), "clientId was empty");
		checkArgument(!timeout.isPresent() || ( timeout.get() > 0 && timeout.get() < 24*60),"timeout expected > 0 and < 24*60 minutes, but was %s",timeout.orNull());
		
		String token = null;
		// create a channel id with the format "businessId|clientId"
		clientId = buildCockpitClientId(business.getId(), clientId);
		
		logger.debug("creating channel for channelID: " +clientId);
		token = (timeout.isPresent())?channelService.createChannel(clientId, timeout.get()):channelService.createChannel(clientId);

		return token;
	}
	
	/**
	 * Create a new message channel for push notification, with the given timeout.
	 * 
	 * @param businessId
	 * @param clientId
	 * @param timeout
	 * @return the token to send to the client
	 * @throws ChannelFailureException if channel creation failed
	 * @throws IllegalArgumentException if unknown businessId has been passed
	 */
	public String createCockpitChannel(long businessId, String clientId , Optional<Integer> timeout) throws ChannelFailureException, IllegalArgumentException {
		checkArgument(businessId != 0, "businessId was 0");
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("unknown businessId", e);
		}
		
		return createCockpitChannel(business, clientId, timeout);
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
		
		if(business.getChannelIds() != null && !business.getChannelIds().isEmpty()) {
			// Send to all clients registered to the business ...
			for (String clientId : business.getChannelIds()) {
				sendMessageRaw(clientId, messageString);
				logger.debug("sent message to channel {} ", clientId);
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
			logger.info("Unsubscribing channel {} from checkin {} ", clientId, checkIn.getNickname());
			checkInRepo.saveOrUpdate(checkIn);
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
			if (business.getChannelIds() == null || business.getChannelIds().isEmpty())	{
				return;
			}
			if(business.getChannelIds().contains(clientId)) {
				business.getChannelIds().remove(clientId);
				logger.info("Unsubscribing channel {} from business {} ", clientId, business.getName());
				businessRepo.saveOrUpdate(business);
			}
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
			logger.error("clientId contains unknown encoded checkIn, clientId={}", clientId, e);
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
		if(business.getChannelIds() == null)
			business.setChannelIds(new ArrayList<String>());
		if(!business.getChannelIds().contains(clientId)) {
			business.getChannelIds().add(clientId);
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
		
		logger.debug("creating channel for channelID: " +clientId);
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

}
