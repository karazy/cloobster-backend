package net.eatsense.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.Request;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.event.DeleteCheckInEvent;
import net.eatsense.event.MoveCheckInEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class MessageController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChannelController channelCtrl;
	private Transformer transform;
	private CheckInRepository checkInRepo;
	private RequestRepository requestRepo;
	
	@Inject
	public MessageController(ChannelController channelCtrl, Transformer transform, CheckInRepository checkInRepo,
			RequestRepository requestRepo) {
		super();
		this.requestRepo = requestRepo;
		this.checkInRepo = checkInRepo;
		this.transform = transform;
		this.channelCtrl = channelCtrl;
	}


	@Subscribe
	public void sendNewCheckInMessages(NewCheckInEvent event) {
		logger.info("New checkin event recieved for {}", event.getCheckIn().getNickname());
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getCheckIn().getSpot().getId());
		spotData.setCheckInCount(event.getCheckInCount().or(
				checkInRepo.countActiveCheckInsAtSpot(event.getCheckIn().getSpot())));

		// Notify cockpit clients
		List<MessageDTO> messages = new ArrayList<MessageDTO>();		
		messages.add(new MessageDTO("spot","update",spotData));
		messages.add(new MessageDTO("checkin","new", transform.toStatusDto(event.getCheckIn())));
		channelCtrl.sendMessagesToAllCockpitClients(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendDeleteCheckInMessages(DeleteCheckInEvent event) {
		logger.info("Delete checkin event recieved for {}", event.getCheckIn().getNickname());
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getCheckIn().getSpot().getId());		
		spotData.setCheckInCount(event.getCheckInCount().or(
				checkInRepo.countActiveCheckInsAtSpot(event.getCheckIn().getSpot())));
		// If the customer didn't check out himself.
		if(!event.isCheckOut()) {
			if ( spotData.getCheckInCount() > 0) {
				Request request = requestRepo.query().filter("spot",event.getCheckIn().getSpot()).order("-receivedTime").get();
				// Save the status of the next request in line, if there is one.
				if( request != null) {
					spotData.setStatus(request.getStatus());
				}
				else 
					spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
			}
			// notify client
			if(event.getCheckIn().getChannelId() != null)
				channelCtrl.sendMessage(event.getCheckIn().getChannelId(), 
						"checkin", "delete", transform.checkInToDto(event.getCheckIn()));
		}
		
		// Notify cockpit clients
		List<MessageDTO> messages = new ArrayList<MessageDTO>();
		messages.add(new MessageDTO("spot", "update", spotData));
		messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(event.getCheckIn())));
		channelCtrl.sendMessagesToAllCockpitClients(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendMoveCheckInMessages(MoveCheckInEvent event) {
		logger.info("Move checkin event recieved for {}", event.getCheckIn().getNickname());
		List<MessageDTO> messages = new ArrayList<MessageDTO>();
		// Create status update messages for listening channels
		CheckInStatusDTO oldCheckInStatus = transform.toStatusDto(event.getCheckIn());
		oldCheckInStatus.setSpotId(event.getOldSpot().getId());
		messages.add(new MessageDTO("checkin","delete", oldCheckInStatus));
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getOldSpot().getId());
		spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(event.getOldSpot()));
		
		if(spotData.getCheckInCount() > 0 ) {
			Request request = requestRepo.query().filter("spot",event.getOldSpot()).order("-receivedTime").get();
			// Save the status of the next request in line, if there is one.
			if( request != null) {
				spotData.setStatus(request.getStatus());
			}
			else
				spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
		}
		messages.add(new MessageDTO("spot", "update", spotData));
		// Add message with new checkin status data.
		messages.add(new MessageDTO("checkin","new", transform.toStatusDto(event.getCheckIn())));
		spotData = new SpotStatusDTO();
		spotData.setId(event.getCheckIn().getSpot().getId());
		spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(event.getCheckIn().getSpot()));
				
		Request request = requestRepo.query().filter("spot",event.getCheckIn().getSpot()).order("-receivedTime").get();
		// Save the status of the next request in line, if there is one.
		if( request != null) {
			spotData.setStatus(request.getStatus());
		}
		else
			spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
		
		messages.add(new MessageDTO("spot", "update", spotData));
		
		channelCtrl.sendMessagesToAllCockpitClients(event.getBusiness(), messages);
	}
}
