package net.eatsense.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	
	public String createChannel(Long restaurantId, String clientId) {
		return createChannel(restaurantId, clientId, null);
	}
	
	public String createChannel(Restaurant restaurant, String clientId) {
		return createChannel(restaurant, clientId, null);
	}
	
	public String createChannel(Restaurant restaurant, String clientId , Integer timeout) {
		String token = null;
		
		clientId += new Date().getTime();
		logger.debug("creating channel for channelID: " +clientId);
		token = (timeout != null)?channelService.createChannel(clientId, timeout):channelService.createChannel(clientId);
		
		if(restaurant.getChannelIds() == null) {
			restaurant.setChannelIds(new ArrayList<String>());
		}
		
		restaurant.getChannelIds().add(clientId);
		restaurantRepo.saveOrUpdate(restaurant);
		return token;
	}
	
	public String createChannel(Long restaurantId, String clientId , Integer timeout) {
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
				
		if(restaurant.getChannelIds() == null) {
			restaurant.setChannelIds(new ArrayList<String>());
		}	
		return createChannel(restaurant, clientId, timeout);
	}
	
	public String sendMessage(String clientId, String type, String action, Object content) throws Exception {
		MessageDTO messageData = new MessageDTO();
		String messageString;
		messageData.setAction(action);
		messageData.setType(type);
		
		messageData.setContent(content);
		
		try {
			messageString = mapper.writeValueAsString(messageData);
		}  catch (Exception e) {
			throw e;
		} 
		
		ChannelMessage message = new ChannelMessage(clientId, messageString);
		channelService.sendMessage(message);
		
		return messageString;
	}
	
	public void sendMessageToAllClients(Long restaurantId, String type, String action, Object content) throws Exception {
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
		for (String clientId : restaurant.getChannelIds()) {
			logger.debug(sendMessage(clientId, type, action, content));
		}
	}
}
