package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.event.ConfirmAllOrdersEvent;
import net.eatsense.event.DeleteCheckInEvent;
import net.eatsense.event.DeleteCustomerRequestEvent;
import net.eatsense.event.MoveCheckInEvent;
import net.eatsense.event.NewBillEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.event.NewCustomerRequestEvent;
import net.eatsense.event.PlaceAllOrdersEvent;
import net.eatsense.event.UpdateBillEvent;
import net.eatsense.event.MultiUpdateEvent;
import net.eatsense.event.UpdateOrderEvent;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.representation.CustomerRequestDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class MessageController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChannelController channelCtrl;
	private Transformer transform;
	private CheckInRepository checkInRepo;
	private RequestRepository requestRepo;
	private BusinessRepository businessRepo;
	
	@Inject
	public MessageController(ChannelController channelCtrl, Transformer transform, CheckInRepository checkInRepo,
			RequestRepository requestRepo, BusinessRepository businessRepo) {
		super();
		this.requestRepo = requestRepo;
		this.businessRepo = businessRepo;
		this.checkInRepo = checkInRepo;
		this.transform = transform;
		this.channelCtrl = channelCtrl;
	}
	
	@Subscribe
	public void sendConfirmAllOrdersMessages(ConfirmAllOrdersEvent event) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		if(event.getNewSpotStatus().isPresent()) {
			// Add a message with updated spot status to the package.
			SpotStatusDTO spotData = new SpotStatusDTO();
			spotData.setId(event.getCheckIn().getSpot().getId());
			spotData.setStatus(event.getNewSpotStatus().get());
			messages.add(new MessageDTO("spot","update",spotData));
		}
		CheckInStatusDTO statusDto = transform.toStatusDto(event.getCheckIn());
		if(event.getNewCheckInStatus().isPresent()) {
			// ... and add a message with updated checkin status to the package.
			messages.add(new MessageDTO("checkin","update",statusDto));
		}
		messages.add(new MessageDTO("checkin","confirm-orders",statusDto));
		// Send update messages.
		channelCtrl.sendMessages(businessRepo.getByKey(event.getCheckIn().getBusiness()), messages);
	}
	
	@Subscribe
	public void sendPlaceAllOrdersMessages(PlaceAllOrdersEvent event) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		if(event.getNewSpotStatus().isPresent()) {
			// Add a message with updated spot status to the package.
			SpotStatusDTO spotData = new SpotStatusDTO();
			spotData.setId(event.getCheckIn().getSpot().getId());
			spotData.setStatus(event.getNewSpotStatus().get());
			messages.add(new MessageDTO("spot","update",spotData));
		}
		CheckInStatusDTO statusDto = transform.toStatusDto(event.getCheckIn());
		if(event.getNewCheckInStatus().isPresent()) {
			// ... and add a message with updated checkin status to the package.
			messages.add(new MessageDTO("checkin","update",statusDto));
		}
		messages.add(new MessageDTO("checkin","update-orders",statusDto));
		// Send update messages.
		channelCtrl.sendMessages(businessRepo.getByKey(event.getCheckIn().getBusiness()), messages);
	}
	
	@Subscribe
	public void sendDeleteCustomerRequestMessages(DeleteCustomerRequestEvent event) {
		List<Request> oldRequests = requestRepo.query().filter("spot",event.getRequest().getSpot()).order("-receivedTime").limit(2).list();
		for (Iterator<Request> iterator = oldRequests.iterator(); iterator.hasNext();) {
			Request oldRequest = iterator.next();
			if(event.getRequest().getId().equals(oldRequest.getId())) {
				iterator.remove();
			}			
		}
		
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setCheckInId(event.getRequest().getCheckIn().getId());
		requestData.setId(event.getRequest().getId());
		requestData.setType(event.getRequest().getStatus());
		requestData.setSpotId(event.getRequest().getSpot().getId());
		messages.add(new MessageDTO("request", "delete", requestData));
		
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getRequest().getSpot().getId());
		
		// Save the status of the next request in line, if there is one.
		if( !oldRequests.isEmpty() ) {
			spotData.setStatus(oldRequests.get(0).getStatus());
		}
		else
			spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
		
		messages.add(new MessageDTO("spot", "update", spotData));
		
		channelCtrl.sendMessages(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendNewCustomerRequestMessages(NewCustomerRequestEvent event ) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getCheckIn().getSpot().getId());
		spotData.setStatus(event.getRequest().getStatus());
				
		messages.add(new MessageDTO("spot", "update", spotData));
		
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setCheckInId(event.getCheckIn().getId());
		requestData.setId(event.getRequest().getId());
		requestData.setType(event.getRequest().getStatus());
		requestData.setSpotId(event.getCheckIn().getSpot().getId());
		
		messages.add(new MessageDTO("request", "new", requestData ));
		
		channelCtrl.sendMessages(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendNewBillMessages(NewBillEvent event) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		// Add a message with the new bill to the message package.
		messages.add(new MessageDTO("bill","new", transform.billToDto(event.getBill())));
		// Add a message with updated checkin status to the package.
		messages.add(new MessageDTO("checkin","update",transform.toStatusDto(event.getCheckIn())));
		
		if(event.getNewSpotStatus().isPresent()) {
			SpotStatusDTO spotData = new SpotStatusDTO();
			spotData.setId(event.getCheckIn().getSpot().getId());
			spotData.setStatus(event.getNewSpotStatus().get());
			// Add a message with updated spot status to the package.
			messages.add(new MessageDTO("spot", "update", spotData));
		}
		
		// Send messages to notify clients over their channel.
		channelCtrl.sendMessages(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendUpdateBillMessages(UpdateBillEvent event) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();

		// Add a message with updated checkin status to the package.
		messages.add(new MessageDTO("checkin","delete",transform.toStatusDto(event.getCheckIn())));
		
		// Add a message with updated bill status to the message package.
		messages.add(new MessageDTO("bill","update",transform.billToDto(event.getBill())));

		int checkInCount = checkInRepo.countActiveCheckInsAtSpot(event.getCheckIn().getSpot());
		
		// Add a message with updated spot status to the package.
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(event.getCheckIn().getSpot().getId());
		spotData.setCheckInCount(checkInCount);
		if(checkInCount > 0)
			spotData.setStatus(event.getNewSpotStatus().or(CheckInStatus.CHECKEDIN.toString()));
		
		messages.add(new MessageDTO("spot","update",spotData));
		
		channelCtrl.sendMessages(event.getBusiness(), messages);
	}
	
	@Subscribe
	public void sendUpdateOrderMessages(UpdateOrderEvent event) {
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		if(event.getNewSpotStatus().isPresent()) {
			// Add a message with updated spot status to the package.
			SpotStatusDTO spotData = new SpotStatusDTO();
			spotData.setId(event.getCheckIn().getSpot().getId());
			spotData.setStatus(event.getNewSpotStatus().get());
			messages.add(new MessageDTO("spot","update",spotData));
			
		}
		if(event.getNewCheckInStatus().isPresent()) {
			// ... and add a message with updated checkin status to the package.
			messages.add(new MessageDTO("checkin","update",transform.toStatusDto(event.getCheckIn())));
		}
		// Use given order dto or create a new one if not present.
		OrderDTO orderData = event.getOrderData().or(transform.orderToDto(event.getOrder()));
		// Add a message with updated order status to the message package.
		MessageDTO orderMessage = new MessageDTO("order","update",orderData );
		messages.add(orderMessage);
		// Send update messages.
		channelCtrl.sendMessages(event.getBusiness(), messages);
		// If we cancel the order, let the checkedin customer know.
		if(event.getOrder().getStatus() == OrderStatus.CANCELED && event.getCheckIn().getChannelId() != null) {
			orderMessage = new MessageDTO("order","delete",orderData );
			channelCtrl.sendMessage(event.getCheckIn().getChannelId(), orderMessage);
		}
			
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
		channelCtrl.sendMessages(event.getBusiness(), messages);
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
						new MessageDTO("checkin", "delete", transform.checkInToDto(event.getCheckIn())));
		}
		
		// Notify cockpit clients
		List<MessageDTO> messages = new ArrayList<MessageDTO>();
		messages.add(new MessageDTO("spot", "update", spotData));
		messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(event.getCheckIn())));
		channelCtrl.sendMessages(event.getBusiness(), messages);
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
		
		channelCtrl.sendMessages(event.getBusiness(), messages);
	}
}
