package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.validation.CheckInStep2;
import net.eatsense.event.CheckInEvent;
import net.eatsense.event.DeleteCheckInEvent;
import net.eatsense.event.MoveCheckInEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.exceptions.CheckInFailureException;
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
import net.eatsense.util.IdHelper;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
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
	private SpotRepository spotRepo;
	private Transformer transform;
	private ObjectMapper mapper;
    private Validator validator;
	private RequestRepository requestRepo;
	private OrderRepository orderRepo;
	private EventBus eventBus;

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
	public CheckInController(BusinessRepository businessRepository,
			CheckInRepository checkInRepository, SpotRepository spotRepository,
			Transformer transformer,
			ObjectMapper objectMapper, Validator validator,
			RequestRepository requestRepository, OrderRepository orderRepo,
			EventBus eventBus) {
		this.businessRepo = businessRepository;
		this.eventBus = eventBus;
		this.checkInRepo = checkInRepository;
		this.spotRepo = spotRepository;
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
    public SpotDTO getSpotInformation(String barcode) {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	
    	Spot spot = spotRepo.getByProperty("barcode", barcode);
    	if(spot == null )
    		return null;
    	
    	SpotDTO spotDto = toSpotDto(spot);
    	
		return spotDto ;
    }

    public SpotDTO toSpotDto(Spot spot) {
		Business business = businessRepo.getByKey(spot.getBusiness());
    	SpotDTO spotDto = new SpotDTO();    	
    	spotDto.setBarcode(spot.getBarcode());
    	spotDto.setName(spot.getName());
    	spotDto.setBusiness(business.getName());
    	spotDto.setBusinessId(business.getId());
    	spotDto.setPayments(business.getPaymentMethods());
    	spotDto.setGroupTag(spot.getGroupTag());
		return spotDto;
	}
	
	public CheckInDTO toDto(CheckIn checkIn) {
		return transform.checkInToDto(checkIn);
	}

	/**
	 * Create and save a new checkin in the store.
	 * 
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO createCheckIn(CheckInDTO checkInDto) {
		String message = null;
		
		if(checkInDto == null ) {
			throw new IllegalArgumentException("Unable to create checkin, data is null");
		}
		if( checkInDto.getStatus()==null || checkInDto.getStatus() != CheckInStatus.INTENT ) {
			throw new IllegalArgumentException("Unable to create checkin, status should be INTENT but is " + checkInDto.getStatus());
		}
			
		// Find spot by the given barcode
		Spot spot = spotRepo.getByProperty("barcode", checkInDto.getSpotId());
		if(spot == null )
    		throw new IllegalArgumentException("Unable to create checkin, spot barcode unknown");
    	
    	Business business;
		try {
			business = businessRepo.getByKey(spot.getBusiness());
		} catch (com.googlecode.objectify.NotFoundException e1) {
			throw new IllegalArgumentException("Unable to create checkin, business id unknown", e1);
		}
    	
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
				throw new IllegalArgumentException(message);
					
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
					throw new CheckInFailureException(message);
				}
			}
		}

		checkInRepo.saveOrUpdate(checkIn);
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);
		
		// send the event
		CheckInEvent newCheckInEvent = new NewCheckInEvent(business, checkIn);
		newCheckInEvent.setCheckInCount(checkInCount);
		eventBus.post(newCheckInEvent);
		return checkInDto;
	}

	/**
	 * Shows a list of all other checkedIn Users at the same spot.
	 * 
	 * @param spotBarcode
	 * @param checkInId
	 * @return List of user objects
	 */
	public List<User> getOtherUsersAtSpot(CheckIn checkIn, String spotBarcode) {
		List<User> usersAtSpot = new ArrayList<User>();
		if(checkIn == null) 
			return usersAtSpot;
		
		Spot spot = spotRepo.getByKey(checkIn.getSpot());
		if(! spot.getBarcode().equals(spotBarcode))
			return usersAtSpot;
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(spot.getKey());
		
		if (checkInsAtSpot != null && !checkInsAtSpot.isEmpty()) {
			usersAtSpot = new ArrayList<User>();
			
			// Other users at this spot exist.
			for (CheckIn otherCheckIn : checkInsAtSpot) {
				
				if(!otherCheckIn.getId().equals(checkIn.getId()) && isPaymentLinkPossible(otherCheckIn)) {
					User user = new User();
					
					user.setUserId(otherCheckIn.getUserId());
					user.setNickname(otherCheckIn.getNickname());
					
					usersAtSpot.add(user);
				}
			}
		}

		return usersAtSpot;
	}
	
	/**
	 * Update existing checkIn
	 * 
	 * @param checkIn
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO updateCheckIn(CheckIn checkIn, CheckInDTO checkInDto) {
		if(checkIn == null )
			throw new IllegalArgumentException("checkIn is null");
		boolean save = false;
		
		if(checkInDto.getLinkedCheckInId() != null && !checkInDto.getLinkedCheckInId().equals(checkIn.getLinkedUserId())) {
			CheckIn checkInLinkedUser = checkInRepo.getByProperty("userId", checkInDto.getLinkedCheckInId());
			if(checkInLinkedUser == null )
				throw new IllegalArgumentException("Cannot update checkin, linkedCheckInId unknown");
			
			if(checkIn != null && checkInLinkedUser != null) {
				if (checkIn.getStatus() == CheckInStatus.CHECKEDIN && isPaymentLinkPossible(checkInLinkedUser)) {
					checkIn.setLinkedUserId(checkInLinkedUser.getUserId());
					save = true;
				}
				else
					throw new CheckInFailureException("Cannot update checkin, unable to link to given checkin");
			}
		}
		//TODO: allow updating of other fields, like nickname

		if(save) {
			checkInRepo.saveOrUpdate(checkIn);
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
	 * @param checkInUid
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin otherwise <code>null</code>
	 */
	public CheckIn getCheckIn(String checkInUid) {
		return checkInRepo.getByProperty("userId", checkInUid);
	}
	
	/**
	 * Helper method for condition checks of an existing CheckIn
	 * 
	 * @param CheckIn object which needs to be checked available linking
	 * @return TRUE if all conditions are met so this existing checked in user available for payment linking
	 */
	private boolean isPaymentLinkPossible(CheckIn existingCheckIn) {
		
		return (!existingCheckIn.isArchived() &&
				existingCheckIn.getLinkedUserId() == null && 
				existingCheckIn.getStatus() != CheckInStatus.INTENT && 
				existingCheckIn.getStatus() != CheckInStatus.PAYMENT_REQUEST);
	}

	/**
	 * Delete the checkIn from database only if there are no orders placed or payment requested.
	 * 
	 * @param checkInUid
	 */
	public void checkOut(CheckIn checkIn) {
		if(checkIn == null) {
			throw new IllegalArgumentException("Unable to delete checkin, checkin null.");
		}
		
		if(checkIn.getStatus() == CheckInStatus.ORDER_PLACED || checkIn.getStatus() == CheckInStatus.PAYMENT_REQUEST) {
			throw new CheckInFailureException("Unable to delete checkin, order or payment in progress");
		}
		else {
			
			checkInRepo.ofy().delete(checkInRepo.ofy().query(Order.class).filter("status", "CART").listKeys());
			checkInRepo.delete(checkIn);
			
			eventBus.post(new DeleteCheckInEvent(checkIn, businessRepo.getByKey(checkIn.getBusiness()), true));
		}			
	}

	/**
	 * Get status of all active checkins at the given spot.
	 * 
	 * @param business
	 * @param spotId
	 * @return collection of checkin status DTOs
	 */
	public Collection<CheckInStatusDTO> getCheckInStatusesBySpot(Business business, Long spotId) {
		return transform.toStatusDtos( getCheckInsBySpot(business, spotId));
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
	 * @param business
	 * @param spotId
	 * @return
	 */
	private List<CheckIn> getCheckInsBySpot(Business business, Long spotId) {
		return getCheckInsBySpot(Spot.getKey(business.getKey(), spotId));
	}

	/**
	 * Delete the checkin and all related entities.
	 * @param business 
	 * 
	 * @param checkInId
	 */
	public void deleteCheckIn(Business business, Long checkInId) {
		if(checkInId == null)
			throw new IllegalArgumentException("Unable to delete checkIn, checkInId is null");
		
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("Unable to delete checkIn, unknown checkInId",e);
		}
		if(checkIn.getBusiness().getId() != business.getId()) {
			throw new IllegalArgumentException("Unable to delete checkIn, checkIn does not belong to business");
		}
		
		Objectify ofy = checkInRepo.ofy();
					
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
		
		// Send event
		eventBus.post(new DeleteCheckInEvent(checkIn, business, false));
	}

	/**
	 * Update a checkin to move to a new spot.
	 * @param business 
	 * 
	 * @param checkInId
	 * @param checkInData
	 * @return updated checkin data
	 */
	public CheckInStatusDTO updateCheckInAsBusiness(Business business, Long checkInId, CheckInStatusDTO checkInData) {
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("Unable to update checkin, unknown ckeckInId",e);
		}
		if(checkIn.getBusiness().getId() != business.getId()) {
			throw new IllegalArgumentException("Unable to update checkIn, checkIn does not belong to business");
		}
		Objectify ofy = checkInRepo.ofy();
			
		Key<Spot> oldSpotKey = checkIn.getSpot();
		Key<Spot> newSpotKey = Spot.getKey(checkIn.getBusiness(), checkInData.getSpotId());
		// Check if spot has changed ...
		if(oldSpotKey.getId() != newSpotKey.getId()) {
			// ... then we move the checkin to a new spot.
			
			checkIn.setSpot(newSpotKey);
			checkInRepo.saveOrUpdate(checkIn);
			
			// Get all pending requests of this user.
			List<Request> requests = ofy.query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();
			for (Request request : requests) {
				// Update the requests for the new spot.
				request.setSpot(newSpotKey);
			}
			requestRepo.saveOrUpdate(requests);
			
			// Send move event
			eventBus.post(new MoveCheckInEvent(checkIn, business, oldSpotKey));			
		}
		
		return checkInData;
	}
}
