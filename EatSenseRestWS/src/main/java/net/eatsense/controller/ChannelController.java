package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Business;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.cockpit.MessageDTO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;

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
	
	@Inject
	public ChannelController(BusinessRepository rr) {
		super();
		this.businessRepo = rr;
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
	public String createChannel(Long businessId, String clientId) {
		return createChannel(businessId, clientId, null);
	}
	
	/**
	 * Create a new message channel for push notification, with a default timeout of 2h,
	 * and register the client id in the business.
	 * 
	 * @param business datastore entity
	 * @param clientId
	 * @return the token to send to the client
	 */
	public String createChannel(Business business, String clientId) {
		return createChannel(business, clientId, null);
	}
	
	
	/**
	 * Create a new message channel for push notification, with the given timeout.
	 * 
	 * @param business datastore entity
	 * @param clientId
	 * @param timeout in minutes
	 * @return the token to send to the client
	 */
	public String createChannel(Business business, String clientId , Integer timeout) {
		String token = null;
		// create a channel id with the format "businessId|clientId"
		clientId = business.getId() + "|" + clientId;
		
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
	 */
	public String createChannel(Long businessId, String clientId , Integer timeout) {
		Business business = businessRepo.getById(businessId);
		if(business == null)
			throw new IllegalArgumentException("unknown businessId: " + businessId);
		return createChannel(business, clientId, timeout);
	}
	
	/**
	 * Send a message to the client identified by the client id.
	 * 
	 * @param clientId
	 * @param type
	 * @param action 
	 * @param content an object for data serialization to JSON
	 * @return the complete message string 
	 * @throws IOException, JsonGenerationException, JsonMappingException 
	 */
	public String sendMessage(String clientId, String type, String action, Object content) throws IOException, JsonGenerationException, JsonMappingException  {
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
	 * @throws IOException, JsonGenerationException, JsonMappingException 
	 */
	public String sendMessage(String clientId, MessageDTO messageData) throws IOException, JsonGenerationException, JsonMappingException  {
		ChannelMessage message = new ChannelMessage(clientId, buildJson(messageData));
		channelService.sendMessage(message);
		
		return message.getMessage();
	}
	

	private String buildJson(Object object)  throws IOException, JsonGenerationException, JsonMappingException{
		if(object == null) {
			return null;
		}
			
		String messageString;
		
		try {
			// Try to map the message data as a JSON string.
			messageString = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			throw e;
		} catch (JsonMappingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		if(messageString.length() > 32786)
			throw new IllegalArgumentException("data package too long, reduce content size");
		
		return messageString;
	}
	
	
	/**
	 * Send message with only the given string.
	 *  
	 * @param clientId identifying the client(channel) to send to
	 * @param messageString the data to send with the message, limited to 32 kb as UTF-8 string
	 * @return
	 */
	public ChannelMessage sendMessageRaw(String clientId, String messageString) {
		ChannelMessage message = new ChannelMessage(clientId, messageString);
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
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public void sendMessageToAllClients(Long businessId, String type, String action, Object content) throws JsonGenerationException, JsonMappingException, IOException  {
		sendJsonToAllClients(businessId, new MessageDTO(type, action, content));
	}
	
	/**
	 * Send a list of messages as one package over the channel as a JSON array.
	 * 
	 * @param businessId
	 * @param messages
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void sendMessagesToAllClients(Long businessId, List<MessageDTO> content) throws JsonGenerationException, JsonMappingException, IOException  {
		sendJsonToAllClients(businessId, content);
	}
	
	private void sendJsonToAllClients(Long businessId, Object content)
			throws JsonGenerationException, JsonMappingException, IOException  {
		Business business = businessRepo.getById(businessId);
		String messageString = buildJson(content);
		logger.debug("sending message {} ...", messageString);
		
		if(business.getChannelIds() != null) {
			
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
			logger.error("could not parse presence", e);
			throw new RuntimeException(e);
		}
		logger.debug("recieved disconnected from clientId:" + clientId);
		unsubscribeClient(clientId);
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
			logger.error("could not parse presence", e);
			throw new RuntimeException(e);
		}
		logger.debug("recieved connected from clientId:" + clientId);
		subscribeToBusiness(clientId);
	}
	
	/**
	 * Remove the channel from the business<br>
	 * It doesnt recieve further messages, concerning this business.
	 * 
	 * @param clientId
	 */
	public void unsubscribeClient(String clientId) {
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
		Long businessId = Long.valueOf(clientId.substring(0, clientId.indexOf("|") ));
		
		Business business = businessRepo.getById(businessId);
				
		return business;
	}

	
	/**
	 * Save the given client as a listener to updates for this business.
	 * 
	 * @param clientId
	 */
	public void subscribeToBusiness( String clientId) {
		Business business = parseBusiness(clientId);
		
		if(business == null) {
			throw new IllegalArgumentException("unknown businessId encoded in clientId: "+ clientId);
		}
		else { 
			if(business.getChannelIds() == null)
				business.setChannelIds(new ArrayList<String>());
			business.getChannelIds().add(clientId);
			businessRepo.saveOrUpdate(business);
		}
	}

}
