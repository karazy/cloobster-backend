package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Request;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CustomerRequestDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Query;
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
			throw new IllegalArgumentException("Cant post request for archived checkin");
		
		if( ! "CALL_WAITER".equals(requestData.getType()))
			throw new IllegalArgumentException("Unrecognized request type: " + requestData.getType());
		
		requestData.setCheckInId(checkIn.getId());
		requestData.setSpotId(checkIn.getSpot().getId());
		
		List<Request> requests = requestRepo.ofy().query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();		
		
		for (Request oldRequest : requests) {
			if(oldRequest.getType() == RequestType.CUSTOM && oldRequest.getStatus().equals("CALL_WAITER")) {
				logger.info("{} already registered.", oldRequest.getKey());
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
		Request oldRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").get();
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(checkIn.getSpot().getId());
		
		// Save the status of the next request in line, if there is one.
		if( oldRequest != null) {
			spotData.setStatus(oldRequest.getStatus());
		}
		else
			spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
				
		messages.add(new MessageDTO("spot", "update", spotData));
		
		messages.add(new MessageDTO("request", "new", requestData));
		
		try {
			channelCtrl.sendMessagesToAllCockpitClients(checkIn.getBusiness().getId(), messages);
		} catch (Exception e) {
			logger.error("error sending messages", e);
		}
										
		return requestData;
	}
	
	/**
	 * Get outstanding CALL_WAITER requests for the given business and optionally checkin or spot.
	 * 
	 * @param businessId
	 * @param checkInId can be null
	 * @param spotId can be null
	 * @return request DTO
	 */
	public List<CustomerRequestDTO> getCustomerRequestData(Long businessId, Long checkInId, Long spotId) {
		List<CustomerRequestDTO> requestDataList = new ArrayList<CustomerRequestDTO>();
		Query<Request> query = requestRepo.ofy().query(Request.class).ancestor(Business.getKey(businessId));

		if( spotId != null) {
			query = query.filter("spot", Spot.getKey(Business.getKey(businessId), spotId));
		}
		
		if( checkInId != null ) {
			query = query.filter("checkIn", CheckIn.getKey(checkInId));
		}
		
		List<Request> requests = query.list();		
		for (Request request : requests) {
			if(request.getType() == RequestType.CUSTOM && request.getStatus().equals("CALL_WAITER")) {
				
				CustomerRequestDTO requestData = new CustomerRequestDTO();
				requestData.setId(request.getId());
				requestData.setCheckInId(request.getCheckIn().getId());
				requestData.setSpotId(request.getSpot().getId());
				requestData.setType(request.getStatus());
				
				requestDataList.add(requestData);
			}
				
		}
		if(requestDataList.isEmpty())
			return null;
		else
			return requestDataList;
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
		requestData.setSpotId(request.getSpot().getId());

		requestRepo.delete(request);
		
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
		messages.add(new MessageDTO("request", "delete", requestData));
		Request oldRequest = requestRepo.ofy().query(Request.class).filter("spot",request.getSpot()).order("-receivedTime").get();
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(request.getSpot().getId());
		
		// Save the status of the next request in line, if there is one.
		if( oldRequest != null) {
			spotData.setStatus(oldRequest.getStatus());
		}
		else
			spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
		
		messages.add(new MessageDTO("spot", "update", spotData));
		
		try {
			channelCtrl.sendMessagesToAllCockpitClients(request.getBusiness().getId(), messages);
		} catch (Exception e) {
			logger.error("error sending messages", e);
		}
	}
}
