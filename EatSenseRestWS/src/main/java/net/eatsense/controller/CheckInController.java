package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.validation.CheckInStep2;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ErrorDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;
import net.eatsense.util.IdHelper;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.sun.jersey.api.NotFoundException;

/**
 * Controller for checkIn logic and process. When an attempt to checkIn at a
 * business is made, various validations have must be executed.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInController {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private BusinessRepository businessRepo;
	private CheckInRepository checkInRepo;
	private SpotRepository barcodeRepo;
	private Transformer transform;
	private ChannelController channelCtrl;
	private ObjectMapper mapper;
    private Validator validator;
	private RequestRepository requestRepo;
	private OrderRepository orderRepo;

	/**
	 * Constructor using injection for creation.
	 * 
	 * @param businessRepository
	 * @param checkInRepository
	 * @param spotRepository
	 * @param transformer
	 * @param channelController
	 * @param objectMapper
	 * @param validator
	 */
	@Inject
	public CheckInController(BusinessRepository businessRepository, CheckInRepository checkInRepository, SpotRepository spotRepository,
			Transformer transformer, ChannelController channelController, ObjectMapper objectMapper, Validator validator, RequestRepository requestRepository, OrderRepository orderRepo) {
		this.businessRepo = businessRepository;
		this.checkInRepo = checkInRepository;
		this.channelCtrl = channelController;
		this.barcodeRepo = spotRepository;
		this.requestRepo = requestRepository;
		this.orderRepo = orderRepo;
		this.transform = transformer;
		this.mapper = objectMapper;
		this.validator = validator;
	}
	

	

    /**
     * Get spot data for a given barcode.
     * 
     * @param barcode
     * @return SpotDTO containing all relevant data for the client
     * @throws NotFoundException
     */
    public SpotDTO getSpotInformation(String barcode) throws NotFoundException {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	SpotDTO spotDto = new SpotDTO();
    	Spot spot = barcodeRepo.getByProperty("barcode", barcode);
    	if(spot == null )
    		return null;
    	
    	Business business = businessRepo.getByKey(spot.getBusiness());
    	
    	    	
    	spotDto.setBarcode(barcode);
    	spotDto.setName(spot.getName());
    	spotDto.setBusiness(business.getName());
    	spotDto.setBusinessId(business.getId());
    	spotDto.setPayments(business.getPaymentMethods());
    	spotDto.setGroupTag(spot.getGroupTag());
    	
		return spotDto ;
    }

	public CheckInDTO createCheckIn(CheckInDTO checkInDto) {
		String message = null;
		
		if(checkInDto == null ) {
			throw new IllegalArgumentException("Unable to create checkin, data is empty");
		}
		if( checkInDto.getStatus()==null || checkInDto.getStatus() != CheckInStatus.INTENT ) {
			throw new IllegalArgumentException("Unable to create checkin, status should be INTENT but is " + checkInDto.getStatus());
		}
			
		// set values for domain object
		Spot spot = barcodeRepo.getByProperty("barcode", checkInDto.getSpotId());
		if(spot == null )
    		throw new IllegalArgumentException("Unable to create checkin, spot barcode unknown");
    	
    	Business business = businessRepo.getByKey(spot.getBusiness());
    	
    	CheckIn checkIn = new CheckIn();
    	
    	String checkInId = IdHelper.generateId();
		checkIn.setBusiness(business.getKey());
		checkIn.setSpot(spot.getKey());
		checkIn.setUserId(checkInId);
		checkIn.setStatus(CheckInStatus.CHECKEDIN);
		checkIn.setCheckInTime(new Date());
		checkIn.setDeviceId(checkInDto.getDeviceId());
		checkIn.setNickname(checkInDto.getNickname());

		// validation 
		Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn, Default.class, CheckInStep2.class);
		// check for validation errors ...
		if( !constraintViolations.isEmpty() )  {
			// constraint violations occurred setting status and logging error
			logger.info("CheckIn validation failed. Message(s):");
			for (ConstraintViolation<CheckIn> violation : constraintViolations) {
				
				logger.info( violation.getPropertyPath() + ": " +violation.getMessage() );
				ErrorDTO errorDto;
				if(violation.getPropertyPath().toString().equals("nickname")) {
					errorDto = new ErrorDTO("checkInErrorNickname", "3","20");
				}
				else {
					errorDto = new ErrorDTO("checkInError", violation.getPropertyPath().toString() + " " + violation.getMessage());
				}
				
				try {
					message = mapper.writeValueAsString(errorDto);
				} catch (Exception e) {
					throw new RuntimeException("error while mapping error data",e);
				}
				throw new RuntimeException(message);
					
			}
		}			
 		
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(checkIn.getSpot());
		// count checkins at spot
		int checkInCount = 1;
		if(checkInsAtSpot != null) {
			Iterator<CheckIn> it = checkInsAtSpot.iterator();
			while(it.hasNext()) {
				checkInCount++;
				CheckIn next = it.next();
				
				if(next.getNickname().equals(checkIn.getNickname() ) ) {
					logger.info("Error: checkin with duplicate nickname tried: "+ checkIn.getNickname());
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInErrorNicknameExists", ""));
					} catch (Exception e) {
						throw new RuntimeException("error while mapping error data",e);
					}
					//abort checkin
					throw new RuntimeException(message);
				}
			}
		}

		checkInRepo.saveOrUpdate(checkIn);
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(spot.getId());
		spotData.setCheckInCount(checkInCount);
		
		List<MessageDTO> messages = new ArrayList<MessageDTO>();		
		
		// add the messages we want to send as one package
		messages.add(new MessageDTO("spot","update",spotData));
		messages.add(new MessageDTO("checkin","new", transform.toStatusDto(checkIn)));
		
		// send the messages
		try {
			channelCtrl.sendMessagesToAllCockpitClients(business.getId(), messages);
		} catch (Exception e) {
			logger.error("error sending messages",e);
		}
	
		return checkInDto;
	}

	/**
	 * Step 3 - I (optional) Shows a list of all checkedIn Users at the same
	 * spot.
	 * 
	 * @param checkInId
	 * @return Map<String,String> - key is another users id - value is another
	 *         users nickname If no other users at this spot exist
	 *         <code>null</code>.
	 */
	public List<User> getUsersAtSpot(String spotId, String checkInId) {
		if(spotId == null || spotId.isEmpty()) 
			return null;
		
		List<User> usersAtSpot = null;
		
		Spot spot = barcodeRepo.getByProperty("barcode", spotId);
		
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
		
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(spot.getKey());
		
		if (checkInsAtSpot != null && !checkInsAtSpot.isEmpty()) {
			usersAtSpot = new ArrayList<User>();
			
			// Other users at this spot exist.
			for (CheckIn checkIn : checkInsAtSpot) {
				
				if(!checkIn.getUserId().equals(checkInId) && isPaymentLinkPossible(checkIn)) {
					User user = new User();
					
					user.setUserId(checkIn.getUserId());
					user.setNickname(checkIn.getNickname());
					
					usersAtSpot.add(user);
				}
			}
		}

		return usersAtSpot;
	}
	
	/**
	 * Update existing checkIn
	 * 
	 * @param checkInId
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO updateCheckIn(String checkInId, CheckInDTO checkInDto) {
		CheckIn checkInUser = checkInRepo.getByProperty("userId", checkInId);
		boolean save = false;
		if(checkInUser == null )
			throw new NotFoundException("Unknown checkInId");
		
		if(!checkInUser.getLinkedUserId().equals(checkInDto.getLinkedCheckInId())) {
			CheckIn checkInLinkedUser = checkInRepo.getByProperty("userId", checkInDto.getLinkedCheckInId());
			if(checkInLinkedUser == null )
				throw new IllegalArgumentException("Cannot update checkin, linkedCheckInId unknown");
			
			if(checkInUser != null && checkInLinkedUser != null) {
				if (checkInUser.getStatus() == CheckInStatus.CHECKEDIN && isPaymentLinkPossible(checkInLinkedUser)) {
					checkInUser.setLinkedUserId(checkInLinkedUser.getUserId());
					save = true;
				}
				else
					throw new RuntimeException("Cannot update checkin, unable to link to given checkin");
			}
		}
		//TODO: allow updating of other fields, like nickname

		if(save) {
			checkInRepo.saveOrUpdate(checkInUser);
		}

		return checkInDto;
	}

	/**
	 * Load checkin.
	 * @param checkInId
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin data otherwise <code>null</code>
	 */
	public CheckInDTO getCheckInAsDTO(String checkInId) {
		return transform.checkInToDto(getCheckIn(checkInId));
	}
	
	/**
	 * Load checkin.
	 * @param checkInId
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin otherwise <code>null</code>
	 */
	public CheckIn getCheckIn(String checkInId) {
		return checkInRepo.getByProperty("userId", checkInId);
	}
	
	/**
	 * Helper method for condition checks of an existing CheckIn
	 * 
	 * @param CheckIn object which needs to be checked available linking
	 * @return TRUE if all conditions are met so this existing checked in user available for payment linking
	 */
	private boolean isPaymentLinkPossible(CheckIn existingCheckIn) {
		
		return (existingCheckIn.getLinkedUserId() == null && 
				existingCheckIn.getStatus() != CheckInStatus.INTENT && 
				existingCheckIn.getStatus() != CheckInStatus.PAYMENT_REQUEST);
	}

	/**
	 * Delete the checkIn from database only if there are no orders placed or payment requested.
	 * 
	 * @param checkInUid
	 */
	public void checkOut(String checkInUid) {
		CheckIn checkIn = getCheckIn(checkInUid);
		if(checkIn == null) {
			logger.info("Cannot checkout, unknown checkin uid given.");
			return;
		}
			
		
		if(checkIn.getStatus() == CheckInStatus.ORDER_PLACED || checkIn.getStatus() == CheckInStatus.PAYMENT_REQUEST) {
			throw new RuntimeException("Cannot checkout, order or payment in progress");
		}
		else {
			List<MessageDTO> messages = new ArrayList<MessageDTO>();
			
			checkInRepo.ofy().delete(checkInRepo.ofy().query(Order.class).filter("status", "CART").listKeys());
					
			checkInRepo.delete(checkIn);
			SpotStatusDTO spotData = new SpotStatusDTO();
			
			spotData.setId(checkIn.getSpot().getId());
			
			spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot()));
			
			messages.add(new MessageDTO("spot", "update", spotData));
			messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(checkIn)));
			try {
				channelCtrl.sendMessagesToAllCockpitClients(checkIn.getBusiness().getId(), messages);
			} catch (Exception e) {
				logger.error("error sending messages", e);
			}
		}			
	}

	/**
	 * Get status of all active checkins at the given spot.
	 * 
	 * @param businessId
	 * @param spotId
	 * @return collection of checkin status DTOs
	 */
	public Collection<CheckInStatusDTO> getCheckInStatusesBySpot(Long businessId, Long spotId) {
		return transform.toStatusDtos( getCheckInsBySpot(businessId, spotId));
	}
	
	/**
	 * Retrieve active checkins at the given spot.
	 * 
	 * @param spotKey
	 * @return
	 */
	private List<CheckIn> getCheckInsBySpot(Key<Spot> spotKey) {
		return checkInRepo.ofy().query(CheckIn.class).filter("spot", spotKey).filter("archived", false).list();
	}

	/**
	 * Retrieve active checkins at the given spot.<br>
	 * Convenience method.
	 * 
	 * @param businessId
	 * @param spotId
	 * @return
	 */
	private List<CheckIn> getCheckInsBySpot(Long businessId, Long spotId) {
		return getCheckInsBySpot(Spot.getKey(Business.getKey(businessId), spotId));
	}

	/**
	 * Delete the checkin and all related entities.
	 * 
	 * @param checkInId
	 */
	public void deleteCheckIn(Long checkInId) {
		Objectify ofy = checkInRepo.ofy();
		
		CheckIn checkIn = checkInRepo.getById(checkInId);
		if(checkIn == null) {
			logger.info("Cannot checkout, unknown checkin uid given.");
			return;
		}
			
		// Delete requests
		ofy.delete(ofy.query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).listKeys());
		
		// Get all orders for this checkin ... 
		List<Key<Order>> orderKeys = ofy.query(Order.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).listKeys();
		for (Key<Order> orderKey : orderKeys) {
			// ... , delete the choices for the order ...
			orderRepo.ofy().delete(orderRepo.ofy().query(OrderChoice.class).ancestor(orderKey).listKeys());
		}
		// ... and delete all orders for the checkin.
		ofy.delete(orderKeys);
		
		// Finally delete the checkin.
		checkInRepo.delete(checkIn);
		
		
		List<MessageDTO> messages = new ArrayList<MessageDTO>();
		// Create status update messages for listening channels
		SpotStatusDTO spotData = new SpotStatusDTO();
		
		Request request = ofy.query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").get();
		// Save the status of the next request in line, if there is one.
		if( request != null) {
			spotData.setStatus(request.getStatus());
		}
		else
			spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
		
		spotData.setId(checkIn.getSpot().getId());
		spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot()));
		
		messages.add(new MessageDTO("spot", "update", spotData));
		
		messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(checkIn)));
		// notify client
		channelCtrl.sendMessage(checkIn.getChannelId(), "checkin", "delete", transform.checkInToDto(checkIn));

		channelCtrl.sendMessagesToAllCockpitClients(checkIn.getBusiness().getId(), messages);
	}

	/**
	 * Update a checkin to move to a new spot.
	 * 
	 * @param checkInId
	 * @param checkInData
	 * @return updated checkin data
	 */
	public CheckInStatusDTO updateCheckInAsBusiness(Long checkInId, CheckInStatusDTO checkInData) {
		Objectify ofy = checkInRepo.ofy();
		
		CheckIn checkIn = checkInRepo.getById(checkInId);
		if(checkIn == null) {
			logger.info("unable to update checkin, unknown uid given");
			return null;
		}
			
		List<MessageDTO> messages = new ArrayList<MessageDTO>();
		Key<Spot> oldSpotKey = checkIn.getSpot();
		Key<Spot> newSpotKey = Spot.getKey(checkIn.getBusiness(), checkInData.getSpotId());
		// Check if spot has changed ...
		if(oldSpotKey.getId() != newSpotKey.getId()) {
			// ... then we move the checkin to a new spot.
			// Create status update messages for listening channels
			messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(checkIn)));
			
			checkIn.setSpot(newSpotKey);
			checkInRepo.saveOrUpdate(checkIn);
			
			// Get all pending requests of this user.
			List<Request> requests = ofy.query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();
			for (Request request : requests) {
				request.setSpot(newSpotKey);
			}
			requestRepo.saveOrUpdate(requests);
			
			SpotStatusDTO spotData = new SpotStatusDTO();
			spotData.setId(oldSpotKey.getId());
			spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(oldSpotKey));
					
			Request request = ofy.query(Request.class).filter("spot",oldSpotKey).order("-receivedTime").get();
			// Save the status of the next request in line, if there is one.
			if( request != null) {
				spotData.setStatus(request.getStatus());
			}
			else
				spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
			
			messages.add(new MessageDTO("spot", "update", spotData));
			
			
			messages.add(new MessageDTO("checkin","new", transform.toStatusDto(checkIn)));
			spotData = new SpotStatusDTO();
			spotData.setId(newSpotKey.getId());
			spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(newSpotKey));
					
			request = ofy.query(Request.class).filter("spot",newSpotKey).order("-receivedTime").get();
			// Save the status of the next request in line, if there is one.
			if( request != null) {
				spotData.setStatus(request.getStatus());
			}
			else
				spotData.setStatus(CheckInStatus.CHECKEDIN.toString());
			
			messages.add(new MessageDTO("spot", "update", spotData));
			
			channelCtrl.sendMessagesToAllCockpitClients(checkIn.getBusiness().getId(), messages);
		}
		
		return checkInData;
	}
	
	/**
	 * Generates and returns a new channel token.
	 * 
	 * @param checkInUid unique identifier of the checkin
	 * @param clientId to use for token creation 
	 * @return the generated channel token
	 */
	public String requestToken (String checkInUid) {
		String token = null;
		try {
			token = channelCtrl.createCustomerChannel(checkInUid);
		} catch (IllegalArgumentException e) {
			logger.error("Failed channel creation, Invalid checkInUid "+ checkInUid, e);
			return null;
		}
		return token;
	}
}
