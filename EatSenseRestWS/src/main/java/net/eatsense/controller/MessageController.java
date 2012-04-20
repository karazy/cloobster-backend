package net.eatsense.controller;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.IsNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.event.NewCheckInEvent;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class MessageController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChannelController channelCtrl;
	private Transformer transform;
	private CheckInRepository checkInRepo;
	
	@Inject
	public MessageController(ChannelController channelCtrl, Transformer transform, CheckInRepository checkInRepo) {
		super();
		this.checkInRepo = checkInRepo;
		this.transform = transform;
		this.channelCtrl = channelCtrl;
	}


	@Subscribe
	public void sendNewCheckInMessages(NewCheckInEvent event) {
		logger.info("new checkin event recieved for {}", event.getCheckIn().getNickname());
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getSpot().getId());
		spotData.setCheckInCount(event.getCheckInCount().or(checkInRepo.countActiveCheckInsAtSpot(event.getCheckIn().getSpot())));
		
		List<MessageDTO> messages = new ArrayList<MessageDTO>();		
		
		// add the messages we want to send as one package
		messages.add(new MessageDTO("spot","update",spotData));
		messages.add(new MessageDTO("checkin","new", transform.toStatusDto(event.getCheckIn())));
		
		channelCtrl.sendMessagesToAllCockpitClients(event.getBusiness(), messages);
	}
}
