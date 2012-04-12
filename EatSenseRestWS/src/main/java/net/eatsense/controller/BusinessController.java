package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Request;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CustomerRequestDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;


/**
 * Manages data concerning one business. 
 * 
 * @author Frederik Reifschneider
 *
 */
public class BusinessController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private CheckInRepository checkInRepo;
	private SpotRepository spotRepo;
	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	
	@Inject
	public BusinessController(RequestRepository rr, CheckInRepository cr, SpotRepository sr, ChannelController channelCtrl) {
		this.requestRepo = rr;
		this.spotRepo = sr;
		this.checkInRepo = cr;
		this.channelCtrl = channelCtrl;
	}
	
	/**
	 * Retrieve initial status data of all spots for the given business id.<br>
	 * (mainly used by the Eatsense Cockpit application).
	 * 
	 * @param businessId
	 * @return List of SpotCockpitDTO objects
	 */
	public List<SpotStatusDTO> getSpotStatusData(Long businessId){
		List<Spot> allSpots = spotRepo.getByParent(Business.getKey(businessId));
		List<SpotStatusDTO> spotDtos = new ArrayList<SpotStatusDTO>();
		
		for (Spot spot : allSpots) {
			SpotStatusDTO spotDto = new SpotStatusDTO();
			spotDto.setId(spot.getId());
			spotDto.setName(spot.getName());
			spotDto.setGroupTag(spot.getGroupTag());
			spotDto.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(spot.getKey()));
			Request request = requestRepo.ofy().query(Request.class).filter("spot",spot.getKey()).order("-receivedTime").get();
			
			if(request != null) {
				spotDto.setStatus(request.getStatus());
			}
			
			spotDtos.add(spotDto);
		}
		
		return spotDtos;
	}
	
	/**
	 * Save an outstanding request posted by a checkedin customer.
	 * 
	 * @param checkInId
	 * @param requestData
	 * @return requestData
	 */
	public CustomerRequestDTO saveCustomerRequest(String checkInId,	CustomerRequestDTO requestData) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);		
		if(checkIn == null)
			throw new NotFoundException();
		if(checkIn.isArchived())
			throw new RuntimeException("Cant post request for archived checkin");
		
		requestData.setCheckInId(checkIn.getId());
		
		List<Request> requests = requestRepo.ofy().query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();		
		
		for (Request oldRequest : requests) {
			if(oldRequest.getType() == RequestType.CUSTOM && oldRequest.getStatus().equals("CALL_WAITER")) {
				oldRequest.setReceivedTime(new Date());
				requestData.setId(oldRequest.getId());
				requestRepo.saveOrUpdate(oldRequest);
				return requestData;
			}
				
		}
		
		Request request = new Request();
		request.setBusiness(checkIn.getBusiness());
		request.setCheckIn(checkIn.getKey());
		request.setReceivedTime(new Date());
		request.setSpot(checkIn.getSpot());
		request.setStatus(requestData.getType());
		request.setType(RequestType.CUSTOM);
		
		if( requestRepo.saveOrUpdate(request) == null)
			throw new RuntimeException("Error while saving request");
		
		requestData.setId(request.getId());
		
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		
		messages.add(new MessageDTO("request", "new", requestData));
		
		try {
			channelCtrl.sendMessagesToAllClients(checkIn.getBusiness().getId(), messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
										
		return requestData;
	}
	
	/**
	 * Get an outstanding request for this checkin.
	 * 
	 * @param businessId
	 * @param checkInId
	 * @return request DTO
	 */
	public CustomerRequestDTO getCustomerRequestData(Long businessId, Long checkInId) {
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		CheckIn checkIn = checkInRepo.getById(checkInId);
		if(checkIn.isArchived())
			throw new RuntimeException("Cant get request for archived checkin");
		
		List<Request> requests = requestRepo.ofy().query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();		
		
		for (Request request : requests) {
			if(request.getType() == RequestType.CUSTOM && request.getStatus().equals("CALL_WAITER")) {
				
				requestData.setId(request.getId());
				requestData.setCheckInId(checkIn.getId());
				requestData.setType(request.getStatus());
				return requestData;
			}
				
		}
		return null;
	}

	/**
	 * Clear an outstanding request of the customer.
	 * 
	 * @param businessId
	 * @param requestId
	 */
	public void deleteCustomerRequest(Long businessId, Long requestId) {
		Request request = requestRepo.getById(Business.getKey(businessId), requestId);
		CustomerRequestDTO requestData = new CustomerRequestDTO();

		requestData.setId(requestId);
		requestData.setType(request.getStatus());
		requestData.setCheckInId(request.getCheckIn().getId());

		requestRepo.delete(request);
		
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		messages.add(new MessageDTO("request", "delete", requestData));
		
		try {
			channelCtrl.sendMessagesToAllClients(request.getBusiness().getId(), messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
