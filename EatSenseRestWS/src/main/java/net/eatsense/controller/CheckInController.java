package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
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
     * @return <code>null</code> if not found or SpotDTO containing all relevant data for the client
     */
    public SpotDTO getSpotInformation(String barcode) {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	
    	return toSpotDto(spotRepo.getByProperty("barcode", barcode)) ;
    }

    public SpotDTO toSpotDto(Spot spot) {
    	if(spot == null)
    		return null;
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
		checkNotNull(checkInDto, "checkInDto was null");
		checkNotNull(checkInDto.getSpotId(), "checkInDto spotId was null");
		checkNotNull(checkInDto.getStatus(), "checkInDto status was null");
		checkArgument(!checkInDto.getSpotId().isEmpty(), "checkInDto spotId was empty");
		checkArgument(checkInDto.getStatus() == CheckInStatus.INTENT,
				"checkInDto status expected to be INTENT but was %s", checkInDto.getStatus());
			
		// Find spot by the given barcode
		Spot spot = spotRepo.getByProperty("barcode", checkInDto.getSpotId());
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
		Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn);
		String errorMessage = null;
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
					errorMessage = mapper.writeValueAsString(errorDto);
				} catch (Exception e) {
					throw new RuntimeException("error while mapping error data",e);
				}
				throw new IllegalArgumentException(errorMessage);
					
			}
		}			
 		
		List<CheckIn> checkInsAtSpot = checkInRepo.getBySpot(checkIn.getSpot());
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
						errorMessage = mapper.writeValueAsString(new ErrorDTO("checkInErrorNicknameExists", ""));
					} catch (Exception e) {
						throw new RuntimeException("error while mapping error data",e);
					}
					//abort checkin
					throw new CheckInFailureException(errorMessage);
				}
			}
		}

		checkInRepo.saveOrUpdate(checkIn);
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);
		
		// send the event
		CheckInEvent newCheckInEvent = new NewCheckInEvent(checkIn, business);
		newCheckInEvent.setCheckInCount(checkInCount);
		eventBus.post(newCheckInEvent);
		return checkInDto;
	}

	/**
	 * Shows a list of all other checkedIn Users at the same spot.
	 * NOT IN USE
	 * 
	 * @param spotBarcode
	 * @param checkInId
	 * @return List of user objects
	 */
	public List<User> getOtherUsersAtSpot(CheckIn checkIn, String spotBarcode) {
		List<User> usersAtSpot = new ArrayList<User>();
		if(checkIn == null) 
			return usersAtSpot;
		checkNotNull(checkIn.getSpot(), "checkIn spot was null");
		checkNotNull(checkIn.getId(), "checkIn id was null");
		
		Spot spot = spotRepo.getByKey(checkIn.getSpot());
		if(! spot.getBarcode().equals(spotBarcode))
			return usersAtSpot;
		List<CheckIn> checkInsAtSpot = checkInRepo.getBySpot(checkIn.getSpot());
		
		if (checkInsAtSpot != null && !checkInsAtSpot.isEmpty()) {
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
	 * NOT IN USE AND NOT TESTED
	 * @param checkIn
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO updateCheckIn(CheckIn checkIn, CheckInDTO checkInDto) {
		checkNotNull(checkIn, "checkIn was null");
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
	 * Load checkin.
	 * @param checkInUid
	 * 			UserId of CheckIn to load	
	 * @return
	 * 		found checkin otherwise <code>null</code>
	 */
	public CheckIn getCheckIn(String checkInUid) {
		if(checkInUid == null || checkInUid.isEmpty())
			return null;
		return checkInRepo.getByProperty("userId", checkInUid);
	}

	/**
	 * Delete the checkIn from database only if there are no orders placed or payment requested.
	 * 
	 * @param checkInUid
	 */
	public void checkOut(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(checkIn.getId(), "checkIn id was null");
		checkArgument(checkIn.getId() != 0, "checkIn id was 0");
		checkNotNull(checkIn.getSpot(), "checkIn spot was null");
		checkNotNull(checkIn.getBusiness(), "checkIn business was null");
		checkNotNull(checkIn.getStatus(), "checkIn status was null");
		
		if(checkIn.getStatus() == CheckInStatus.ORDER_PLACED || checkIn.getStatus() == CheckInStatus.PAYMENT_REQUEST) {
			throw new CheckInFailureException("invalid status " + checkIn.getStatus());
		}
		else {
			int checkInCount = checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot());
			orderRepo.delete(orderRepo.getKeysByProperty("status", OrderStatus.CART.toString()));
			checkInRepo.delete(checkIn);
			
			DeleteCheckInEvent event = new DeleteCheckInEvent(checkIn, businessRepo.getByKey(checkIn.getBusiness()), true);
			
			event.setCheckInCount(checkInCount == 0 ? 0 : checkInCount-1 );
			eventBus.post(event);
		}			
	}

	/**
	 * Get status of all active checkins at the given spot.
	 * 
	 * @param business
	 * @param spotId
	 * @return collection of checkin status DTOs
	 */
	public Collection<CheckInStatusDTO> getCheckInStatusesBySpot(Business business, long spotId) {
		checkNotNull(business, "business was null");
		checkArgument(spotId != 0, "spotId was 0");
		return transform.toStatusDtos( checkInRepo.getBySpot(Spot.getKey(business.getKey(), spotId)));
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
		int checkInCount = checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot());
		Objectify ofy = checkInRepo.ofy();
					
		// Delete requests
		ofy.delete(ofy.query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).listKeys());
		
		// Get all orders for this checkin ... 
		List<Key<Order>> orderKeys = ofy.query(Order.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).listKeys();
		for (Key<Order> orderKey : orderKeys) {
			// ... , delete the choices for the order ...
			ofy.delete(orderRepo.ofy().query(OrderChoice.class).ancestor(orderKey).listKeys());
		}
		// ... and delete all orders for the checkin.
		ofy.delete(orderKeys);
		
		// Finally delete the checkin.
		checkInRepo.delete(checkIn);
		
		// Send event
		DeleteCheckInEvent event = new DeleteCheckInEvent(checkIn, business, false);
		
		event.setCheckInCount(checkInCount-1);
		eventBus.post(event);
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
