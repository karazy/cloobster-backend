package net.eatsense.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;

import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.cockpit.MessageDTO;

/**
 * Sends push messages via the Google Channel Service.
 * 
 * @author Nils Weiher
 *
 */
public class ChannelController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RestaurantRepository restaurantRepo;
	
	ChannelService channelService;
	private ObjectMapper mapper;
	
	@Inject
	public ChannelController(RestaurantRepository rr) {
		super();
		this.restaurantRepo = rr;
		channelService = ChannelServiceFactory.getChannelService();	
		mapper = new ObjectMapper();
	}
	
	/**
	 * Create a new message channel for push notification.
	 * 
	 * @param restaurantId numeric id of a restaurant
	 * @param clientId
	 * @return
	 */
	public String createChannel(Long restaurantId, String clientId) {
		return createChannel(restaurantId, clientId, null);
	}
	
	/**
	 * Create a new message channel for push notification, with a default timeout of 2h,
	 * and register the client id in the restaurant.
	 * 
	 * @param restaurant datastore object
	 * @param clientId
	 * @return the token to send to the client
	 */
	public String createChannel(Restaurant restaurant, String clientId) {
		return createChannel(restaurant, clientId, null);
	}
	
	
	/**
	 * Create a new message channel for push notification, with the given timeout.
	 * 
	 * @param restaurant datastore object
	 * @param clientId
	 * @param timeout in minutes
	 * @return the token to send to the client
	 */
	public String createChannel(Restaurant restaurant, String clientId , Integer timeout) {
		String token = null;
		// create a channel id with the format "restaurantId|clientId|timestamp"
		clientId = restaurant.getId() + "|" + clientId;
		clientId = clientId + "|" + new Date().getTime();
		
		logger.debug("creating channel for channelID: " +clientId);
		token = (timeout != null)?channelService.createChannel(clientId, timeout):channelService.createChannel(clientId);
		
		if(restaurant.getChannelIds() == null) {
			restaurant.setChannelIds(new ArrayList<String>());
		}
		
		restaurant.getChannelIds().add(clientId);
		restaurantRepo.saveOrUpdate(restaurant);
		return token;
	}
	
	/**
	 * Create a new message channel for push notification, with the given timeout.
	 * 
	 * @param restaurantId
	 * @param clientId
	 * @param timeout
	 * @return the token to send to the client
	 */
	public String createChannel(Long restaurantId, String clientId , Integer timeout) {
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
		if(restaurant == null)
			throw new IllegalArgumentException("got unknown restaurantId: " + restaurantId);
		return createChannel(restaurant, clientId, timeout);
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
		String messageString;
		messageData.setAction(action);
		messageData.setType(type);
		
		messageData.setContent(content);
		
			try {
				messageString = mapper.writeValueAsString(messageData);
			} catch (JsonGenerationException e) {
				throw e;
			} catch (JsonMappingException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			}
		if(messageString.length() > 32786)
			throw new IllegalArgumentException("data package too long, reduce content size");
		
		ChannelMessage message = new ChannelMessage(clientId, messageString);
		channelService.sendMessage(message);
		
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
	 * @param restaurantId
	 * @param type
	 * @param action
	 * @param content
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public void sendMessageToAllClients(Long restaurantId, String type, String action, Object content) throws JsonGenerationException, JsonMappingException, IOException  {
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
		String lastMessage = "";
		for (String clientId : restaurant.getChannelIds()) {
			lastMessage = sendMessage(clientId, type, action, content);
		}
		logger.debug("last message sent: "+ lastMessage);
	}

	public void handleDisconnected(HttpServletRequest request) {
		try {
			unsubscribeClient(channelService.parsePresence(request).clientId());
		} catch (IOException e) {
			logger.error("could not parse presence", e);
			throw new RuntimeException(e);
		}
	}
	
	public void unsubscribeClient(String clientId) {
		Restaurant restaurant = parseRestaurant(clientId);
		
		if(restaurant == null) {
			throw new IllegalArgumentException("unknown restaurantId encoded in clientId: "+ clientId);
		}
		else {
			if (restaurant.getChannelIds() == null || restaurant.getChannelIds().isEmpty())	{
				return;
			}
				
			for (String subscribedClientId : restaurant.getChannelIds()) {
				
			}
		}
			
		
	}
	
	private Restaurant parseRestaurant(String clientId) {
		// retrieve the restaurantId appended at the front of the client id seperated with a "|"
		Long restaurantId = Long.valueOf(clientId.substring(0, clientId.indexOf("|")-1) );
		
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
				
		return restaurant;
	}

	public void subscribeToRestaurant(Long restaurantId, String clientId) {
		
	}
	
	public void handleConnected(HttpServletRequest request) {
		try {
			channelService.parsePresence(request);
		} catch (IOException e) {
			logger.error("could not parse presence", e);
			throw new RuntimeException(e);
		}
	}
}
