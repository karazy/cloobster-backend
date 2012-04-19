package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.representation.cockpit.MessageDTO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
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
	public ChannelController(BusinessRepository rr, CheckInRepository checkInRepo) {
		super();
		this.businessRepo = rr;
		this.checkInRepo = checkInRepo;
		channelService = ChannelServiceFactory.getChannelService();	
		mapper = new ObjectMapper();
	}
	
	/**
	 * Create a new message channel for push notification.
	 * 
	 * @param businessId id of a business
	 * @param clientId
	 * @return
	 */
	public String createCockpitChannel(Long businessId, String clientId) {
		return createCockpitChannel(businessId, clientId, null);
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
		return createCockpitChannel(business, clientId, null);
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
	public String createCockpitChannel(Business business, String clientId , Integer timeout) throws ChannelFailureException {
		String token = null;
		// create a channel id with the format "businessId|clientId"
		clientId = "b" + business.getId() + "|" + clientId;
		
		logger.debug("creating channel for channelID: " +clientId);
		token = (timeout != null)?channelService.createChannel(clientId, timeout):channelService.createChannel(clientId);

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
	public String createCockpitChannel(Long businessId, String clientId , Integer timeout) throws ChannelFailureException, IllegalArgumentException {
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("unknown businessId", e);
		}
		
		return createCockpitChannel(business, clientId, timeout);
	}
	
	public String sendMessageToCheckIn(Long checkInId, String type, String action, Object content)  {
		MessageDTO messageData = new MessageDTO();
		messageData.setAction(action);
		messageData.setContent(content);
		messageData.setType(type);
		
		return sendMessageToCheckIn(checkInId, messageData);
	}
	
	/**
	 * Send message to th
	 * 
	 * @param checkInId
	 * @param messageData
	 * @return message as string
	 * @throws IllegalArgumentException if unknown checkInId has been passed
	 */
	public String sendMessageToCheckIn(Long checkInId, MessageDTO messageData) throws IllegalArgumentException {
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Failed to send message, unknow checkInId", e);
		}
		return sendMessage(checkIn.getChannelId(), messageData);
	}
	
	
	/**
	 * Send a message to the client identified by the client id.
	 * 
	 * @param clientId
	 * @param type
	 * @param action 
	 * @param content an object for data serialization to JSON
	 * @return the complete message string 
	 */
	public String sendMessage(String clientId, String type, String action, Object content)  {
		MessageDTO messageData = new MessageDTO();
		messageData.setAction(action);
		messageData.setType(type);
		messageData.setContent(content);
		
		return sendMessage(clientId, messageData);
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
		return sendMessageRaw(clientId, buildJsonMessage(messageData)).getMessage();
	}
	

	/**
	 * Build a JSON string from the given POJO. 
	 * 
	 * @param object
	 * @return JSON string representing the object
	 */
	private String buildJsonMessage(Object object) {
		if(object == null) {
			throw new IllegalArgumentException("object is null");
		}
			
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
	public ChannelMessage sendMessageRaw(String clientId, String messageString) {
		if(messageString.length() > 32786)
			throw new IllegalArgumentException("Cannot send message, data package longer than 32kB");

		ChannelMessage message = new ChannelMessage(clientId, messageString);
		logger.info("Sending to client {}, message: {}", clientId, message.getMessage());
		channelService.sendMessage(message);
		
		return message;
	}
	
	/**
	 * Send message to all subscribed channels of this business.
	 * 
	 * @param businessId
	 * @param type
	 * @param action
	 * @param content JSON model
	 */
	public void sendMessageToAllCockpitClients(Business business, String type, String action, Object content) {
		sendJsonToAllCockpitClients(business, new MessageDTO(type, action, content));
	}
	
	/**
	 * Send a list of messages as one package to all subscribed channels of this business as a JSON array.
	 * 
	 * @param businessId
	 * @param messages
	 */
	public void sendMessagesToAllCockpitClients(Business business, List<MessageDTO> content)  {
		if(content != null && !content.isEmpty())
			sendJsonToAllCockpitClients(business, content);
	}
	
	private void sendJsonToAllCockpitClients(Business business, Object content)  {
		if(business == null) {
			throw new IllegalArgumentException("Unable to send message, business is null");
		}
		
		String messageString = buildJsonMessage(content);
		
		if(business.getChannelIds() != null && !business.getChannelIds().isEmpty()) {
			// Send to all clients registered to the business ...
			for (String clientId : business.getChannelIds()) {
				sendMessageRaw(clientId, messageString);
				logger.debug("sent message to channel {} ", clientId);
			}
		}
		else
			logger.info("no open channels");
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
			throw new RuntimeException("could not parse presence",e);
		}
		logger.debug("recieved connected from clientId:" + clientId);
		if(clientId.startsWith("c")) {
			subscribeCheckIn(clientId);
		}
		else if(clientId.startsWith("b"))
			subscribeToBusiness(clientId);
	}
	
	public void subscribeCheckIn(String clientId) {
		CheckIn checkIn = parseCheckIn(clientId);
		checkIn.setChannelId(clientId);
		checkInRepo.saveOrUpdate(checkIn);
	}
	
	public void unsubscribeCheckIn(String clientId) {
		CheckIn checkIn = parseCheckIn(clientId);
		checkIn.setChannelId(null);
		checkInRepo.saveOrUpdate(checkIn);
	}

	/**
	 * Remove the channel from the business<br>
	 * It doesnt recieve further messages, concerning this business.
	 * 
	 * @param clientId
	 */
	public void unsubscribeFromBusiness(String clientId) {
		Business business = parseBusiness(clientId);
		
		if(business == null) {
			throw new IllegalArgumentException("unknown businessId encoded in clientId: "+ clientId);
		}
		else {
			if (business.getChannelIds() == null || business.getChannelIds().isEmpty())	{
				return;
			}
			business.getChannelIds().remove(clientId);
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
		// retrieve the businessId appended at the front of the client id seperated with a "|"
		Long businessId = Long.valueOf(clientId.substring(1, clientId.indexOf("|") ));

		try {
			return businessRepo.getById(businessId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("clientId contains unknown encoded business, clientId=" + clientId, e);
		}
	}
	
	/**
	 * Get the CheckIn entity by the encoded id.
	 * 
	 * @param clientId
	 * @return CheckIn entity
	 */
	private CheckIn parseCheckIn(String clientId) {
		// retrieve the id encoded after the first character in the string
		Long checkInId = Long.valueOf(clientId.substring(1));
		try {
			return checkInRepo.getById(checkInId);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("clientId contains unknown encoded checkIn, clientId=" + clientId, e);
		}
	}
	
	/**
	 * Save the given client as a listener to updates for this business.
	 * 
	 * @param clientId
	 */
	public void subscribeToBusiness( String clientId) {
		Business business = parseBusiness(clientId);
		if(business.getChannelIds() == null)
			business.setChannelIds(new ArrayList<String>());
		business.getChannelIds().add(clientId);
		businessRepo.saveOrUpdate(business);
	}
	
	/**
	 * Create a new message channel for customer push notification, with the given timeout.
	 * 
	 * @param checkInUid uid of the connecting checkin
	 * @param timeout in minutes
	 * @return the token to send to the client
	 */
	public String createCustomerChannel(CheckIn checkIn, Integer timeout) {
		String token = null;
		if(checkIn == null)
			throw new IllegalArgumentException("checkIn is null");			
		// create a channel id with the format "c{checkInId}"
		String clientId = "c"+ checkIn.getId().toString();
		
		logger.debug("creating channel for channelID: " +clientId);
		// request a new token, with or without timeout depending if set
		token = (timeout != null)?channelService.createChannel(clientId, timeout):channelService.createChannel(clientId);

		return token;
	}
	
	/**
	 * Create a new message channel for customer push notification.
	 * 
	 * @param checkInUid uid of the connecting checkin
	 * @return
	 */
	public String createCustomerChannel(CheckIn checkIn) {
		return createCustomerChannel(checkIn, null);
	}

}
