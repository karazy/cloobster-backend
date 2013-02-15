package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Business;
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
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.CheckInHistoryDTO;
import net.eatsense.representation.HistoryStatusDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.VisitDTO;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.util.IdHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

/**
 * Controller for checkIn logic and process. When an attempt to checkIn at a
 * business is made, various validations have must be executed.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInController {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private LocationRepository businessRepo;
	private CheckInRepository checkInRepo;
	private SpotRepository spotRepo;
	private Transformer transform;
    private Validator validator;
	private RequestRepository requestRepo;
	private OrderRepository orderRepo;
	private EventBus eventBus;
	private OrderChoiceRepository orderChoiceRepo;
	private final AreaRepository areaRepo;
	private final AccountRepository accountRepo;
	private final BillRepository billRepo;

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
	public CheckInController(LocationRepository businessRepository,
			CheckInRepository checkInRepository, SpotRepository spotRepository,
			Transformer transformer, Validator validator,
			RequestRepository requestRepository, OrderRepository orderRepo, OrderChoiceRepository orderChoiceRepo, AreaRepository areaRepository,
			EventBus eventBus, AccountRepository accountRepo, BillRepository billRepo) {
		this.businessRepo = businessRepository;
		this.eventBus = eventBus;
		this.checkInRepo = checkInRepository;
		this.spotRepo = spotRepository;
		this.requestRepo = requestRepository;
		this.orderRepo = orderRepo;
		this.transform = transformer;
		this.areaRepo = areaRepository;
		this.validator = validator;
		this.orderChoiceRepo = orderChoiceRepo;
		this.accountRepo = accountRepo;
		this.billRepo = billRepo;
	}

    /**
     * Get spot data for a given barcode.
     * 
     * @param barcode
     * @param checkInResume If not null spot can be loaded
     * @return <code>null</code> if not found or SpotDTO containing all relevant data for the client
     */
    public SpotDTO getSpotInformation(String barcode, boolean checkInResume) {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	
    	Spot spot = spotRepo.getByProperty("barcode", barcode);
    	
    	if(spot == null || (!spot.isActive() && !checkInResume)) {
    		throw new NotFoundException();
    	}
    	
    	if(spot.getArea() == null) {
    		throw new NotFoundException(); 
    	}
    	
    	try {
			Area area = areaRepo.getByKey(spot.getArea());
			if(!area.isActive() && !checkInResume) {
				throw new NotFoundException();
			}

			Business business = businessRepo.getByKey(spot.getBusiness());
			if(business.isTrash()) {
				throw new NotFoundException("Business is locked");
			}
			return new SpotDTO(spot, business, area) ;
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException("Business or area no longer exists.");
		}
    }
    
    /**
     * Calls {@link CheckInController#getSpotInformation(String, boolean)} with checkInResume=false.
     * @param barcode
     * @return
     */
    public SpotDTO getSpotInformation(String barcode) {
    	return getSpotInformation(barcode, false);
    }
	
	/**
	 * Return transfer object containing relevant data for the checkin.
	 * 
	 * @param checkIn
	 * @return CheckIn transfer object
	 */
	public CheckInDTO getCheckInDto(CheckIn checkIn) {
		return transform.checkInToDto(checkIn, true);
	}

	/**
	 * Create and save a new checkin in the store.
	 * 
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO createCheckIn(CheckInDTO checkInDto) {
		return createCheckIn(checkInDto, Optional.<Account>absent());
	}

	/**
	 * Create and save a new checkin in the store.
	 * 
	 * @param checkInDto
	 * @param optAccount Connect this checkin to the account and set as active.
	 * @return
	 */
	public CheckInDTO createCheckIn(CheckInDTO checkInDto, Optional<Account> optAccount) {
		checkNotNull(checkInDto, "checkInDto was null");
		checkNotNull(checkInDto.getSpotId(), "checkInDto spotId was null");
		checkNotNull(checkInDto.getStatus(), "checkInDto status was null");
		checkArgument(!checkInDto.getSpotId().isEmpty(), "checkInDto spotId was empty");
		checkArgument(checkInDto.getStatus() == CheckInStatus.INTENT,
				"checkInDto status expected to be INTENT but was %s", checkInDto.getStatus());
		
		// Find spot by the given barcode
		Spot spot = spotRepo.getByProperty("barcode", checkInDto.getSpotId());
		if(spot == null )
    		throw new ValidationException("Unable to create checkin, spot barcode unknown");
    	
    	Business business = businessRepo.getByKey(spot.getBusiness());
		
    	CheckIn checkIn = new CheckIn();
    	
    	String checkInId = IdHelper.generateId();
		checkIn.setBusiness(business.getKey());
		checkIn.setSpot(spot.getKey());
		checkIn.setArea(spot.getArea());
		// Set the account key if it is present.
		checkIn.setAccount(optAccount.isPresent() ? optAccount.get().getKey() : null);
		checkIn.setUserId(checkInId);
		checkIn.setStatus(CheckInStatus.CHECKEDIN);
		checkIn.setCheckInTime(new Date());
		checkIn.setDeviceId(checkInDto.getDeviceId());
		checkIn.setNickname(checkInDto.getNickname());

		// validation 
		Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn);
		
		// check for validation errors ...
		if( !constraintViolations.isEmpty() )  {
			// constraint violations occurred setting status and logging error
			logger.info("CheckIn validation failed");
			for (ConstraintViolation<CheckIn> violation : constraintViolations) {
				
				if(violation.getPropertyPath().toString().equals("nickname")) {
					throw new CheckInFailureException("nickname too long or too short", "checkInErrorNickname", "3","25");
				}
				else {
					throw new CheckInFailureException("nickname too long or too short", "checkInError",
							violation.getPropertyPath().toString() + " " + violation.getMessage());
				}	
			}
		}
		
		int checkInCount = checkInRepo.countActiveCheckInsAtSpot(spot.getKey());

		// Check for double nickname is deactivated for now.
//		List<CheckIn> checkInsAtSpot = checkInRepo.getBySpot(checkIn.getSpot());
//		// count checkins at spot
//		if(checkInsAtSpot != null) {
//			Iterator<CheckIn> it = checkInsAtSpot.iterator();
//			while(it.hasNext()) {
//				checkInCount++;
//				CheckIn next = it.next();
//				
//				if(next.getNickname().equals(checkIn.getNickname() ) ) {
//					throw new CheckInFailureException("nickname already exists", "checkInErrorNicknameExists");
//				}
//			}
//		}
		
		logger.info("New CheckIn with userId={}", checkIn.getUserId());

		Key<CheckIn> checkInKey = checkInRepo.saveOrUpdate(checkIn);
		if(optAccount.isPresent()) {
			Account account = optAccount.get();
			account.setActiveCheckIn(checkInKey);
			accountRepo.saveOrUpdate(account);
		}
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);
		
		// send the event
		CheckInEvent newCheckInEvent = new NewCheckInEvent(checkIn, business);
		newCheckInEvent.setCheckInCount(checkInCount + 1);
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
	 * {@link #checkOut(CheckIn, Optional)}
	 * @param checkIn
	 */
	public void checkOut(CheckIn checkIn) {
		checkOut(checkIn, Optional.<Account>absent());
	}

	/**
	 * Delete the checkIn from database only if there are no orders placed or payment requested.
	 * 
	 * @param checkInUid
	 */
	public void checkOut(CheckIn checkIn, Optional<Account> optAccount) {
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(checkIn.getId(), "checkIn id was null");
		checkArgument(checkIn.getId() != 0, "checkIn id was 0");
		checkNotNull(checkIn.getSpot(), "checkIn spot was null");
		checkNotNull(checkIn.getBusiness(), "checkIn business was null");
		checkNotNull(checkIn.getStatus(), "checkIn status was null");
		
		
		if(checkIn.getStatus() == CheckInStatus.ORDER_PLACED || checkIn.getStatus() == CheckInStatus.PAYMENT_REQUEST || checkIn.getStatus() == CheckInStatus.COMPLETE) {
			throw new IllegalAccessException("invalid status " + checkIn.getStatus());
		}
		// Check that there were no orders placed.
		if (orderRepo.queryForCheckInAndStatus(checkIn, OrderStatus.COMPLETE,
								OrderStatus.INPROCESS, OrderStatus.PLACED,
								OrderStatus.RECEIVED).getKey() != null) {
			throw new IllegalAccessException("Unable to check out while orders exist");
		}
		
		int checkInCount = checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot());
		List<Key<Order>> orderKeys = orderRepo.getKeysByProperty("checkIn", checkIn);
		List<Key<OrderChoice>> orderChoiceKeys = new ArrayList<Key<OrderChoice>>();
		
		for (Key<Order> orderKey : orderKeys) {
			orderChoiceKeys.addAll(orderChoiceRepo.getKeysByParent(orderKey));
		}
		requestRepo.delete(requestRepo.getKeysByProperty("checkIn", checkIn));
		orderChoiceRepo.delete(orderChoiceKeys);
		orderRepo.delete(orderKeys);
		
		checkIn.setStatus(CheckInStatus.WAS_INACTIVE);
		checkIn.setArchived(true);
		checkInRepo.saveOrUpdate(checkIn);
		
		// Remove active checkIn from the account, if this was authenticated with an user account.
		if(optAccount.isPresent()) {
			optAccount.get().setActiveCheckIn(null);
			accountRepo.saveOrUpdate(optAccount.get());
		}
		
		DeleteCheckInEvent event = new DeleteCheckInEvent(checkIn, businessRepo.getByKey(checkIn.getBusiness()), true);
		event.setCheckInCount(checkInCount == 0 ? 0 : checkInCount-1 );
		eventBus.post(event);
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
	 * @return 
	 */
	public CheckInStatusDTO deleteCheckIn(Business business, long checkInId) {
		checkNotNull(business, "business was null");
		checkNotNull(business.getId(), "business id was null");
		checkArgument(checkInId != 0, "checkInId was 0");
	
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("checkInId unknown",e);
		}
		if(!checkIn.getBusiness().equals(business.getKey())) {
			throw new IllegalArgumentException("checkIn does not belong to business");
		}
		int checkInCount = checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot());
					
		// Delete requests
		requestRepo.delete(requestRepo.getKeysByProperty("checkIn", checkIn));
				
		// Get all orders for this checkin ... 
		List<Key<Order>> orderKeys = orderRepo.getKeysByProperty("checkIn", checkIn);
		List<Key<OrderChoice>> orderChoiceKeys = new ArrayList<Key<OrderChoice>>();
		
		for (Key<Order> orderKey : orderKeys) {
			orderChoiceKeys.addAll(orderChoiceRepo.getKeysByParent(orderKey));
		}
		
		orderChoiceRepo.delete(orderChoiceKeys);
		// ... and delete all orders for the checkin.
		orderRepo.delete(orderKeys);
		
		// Finally delete the checkin.
		checkInRepo.delete(checkIn);
		
		// Remove active checkIn from the account, if this was authenticated with an user account.
		if(checkIn.getAccount() != null) {
			Account account = accountRepo.getByKey(checkIn.getAccount());
			account.setActiveCheckIn(null);
			accountRepo.saveOrUpdate(account);
		}
		
		// Send event
		DeleteCheckInEvent event = new DeleteCheckInEvent(checkIn, business, false);
		
		event.setCheckInCount(checkInCount-1);
		eventBus.post(event);
		
		return transform.toStatusDto(checkIn);
	}

	/**
	 * Update a checkin to move to a new spot.
	 * @param business 
	 * 
	 * @param checkInId
	 * @param checkInData
	 * @return updated checkin data
	 */
	public CheckInStatusDTO updateCheckInAsBusiness(Business business, long checkInId, CheckInStatusDTO checkInData) {
		checkNotNull(business, "business was null");
		checkNotNull(business.getId(), "business id was null");
		checkArgument(checkInId != 0, "checkInId was 0");
		checkNotNull(checkInData, "checkInData was null");
	
		CheckIn checkIn;
		try {
			checkIn = checkInRepo.getById(checkInId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("checkInId unknown",e);
		}
		if(!checkIn.getBusiness().equals(business.getKey())) {
			throw new IllegalArgumentException("checkIn does not belong to business");
		}
			
		// Check if spot has changed ...
		if(checkInData.getSpotId() != null && checkInData.getSpotId() != 0
				&& checkInData.getSpotId() != checkIn.getSpot().getId()) {
			// ... then we move the checkin to a new spot.
			Key<Spot> oldSpotKey = checkIn.getSpot();
			Key<Spot> newSpotKey = spotRepo.getKey(checkIn.getBusiness(), checkInData.getSpotId());

			checkIn.setSpot(newSpotKey);
			checkInRepo.saveOrUpdate(checkIn);
			
			// Get all pending requests of this user.
			List<Request> requests = requestRepo.getListByProperty("checkIn", checkIn);
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
	
	/**
	 * Load information about past checkIns for this account or anonymous installation id.
	 * 
	 * @param account
	 * @param limit 
	 * @param start 
	 * @return
	 */
	public List<VisitDTO> getVisits(Optional<Account> account , String installId, int start, int limit) {
		Query<CheckIn> checkInQuery = checkInRepo.query().order("-id").filter("status", CheckInStatus.COMPLETE).offset(start).limit(limit);
		if(account.isPresent()) {
			checkInQuery = checkInQuery.filter("account", account.get());
		}
		else {
			if(Strings.isNullOrEmpty(installId)) {
				throw new ValidationException("Unable to query without deviceId");
			}
			checkInQuery = checkInQuery.filter("deviceId", installId);
		}
		 
		ArrayList<VisitDTO> visitDTOList = new ArrayList<VisitDTO>();
		
		for (CheckIn checkIn : checkInQuery) {
			Business business = businessRepo.getByKey(checkIn.getBusiness());
			Bill bill = billRepo.getByProperty("checkIn", checkIn);
			visitDTOList.add( new VisitDTO(checkIn, business, bill));
		}
		
		return visitDTOList;
	}
	
	/**
	 * @param account
	 * @param installId
	 * @return
	 */
	public HistoryStatusDTO connectVisits(Account account, HistoryStatusDTO historyDTO) {
		checkNotNull(account, "account was null");
		
		if(Strings.isNullOrEmpty(historyDTO.getInstallId())) {
			throw new ValidationException("installId was null or empty");
		}
		
		Query<CheckIn> visitsQuery = checkInRepo.query().filter("status", CheckInStatus.COMPLETE).filter("deviceId", historyDTO.getInstallId());
		List<CheckIn> visits = new ArrayList<CheckIn>();
		int visitCount = 0;
		for (CheckIn checkIn : visitsQuery) {
			// Only update check which are not yet linked.
			if(checkIn.getAccount() == null) {
				checkIn.setAccount(account.getKey());
				//TODO Think about removing deviceId from the checkIns?
				// Do we want to associate visits with a user and a device, is that a privacy problem?
				visits.add(checkIn);
				++visitCount;
			}
		}
		
		
		// Save all the check ins!
		checkInRepo.saveOrUpdate(visits);
		// Return number of connected checkIns.
		historyDTO.setVisitCount(visitCount);
		return historyDTO;
	}
	
	/**
	 * @param businessKey
	 * @param areaId
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<CheckInHistoryDTO> getHistory(Key<Business> businessKey, long areaId, int start, int limit) {
		checkNotNull(businessKey, "businessKey was null");
		
		Query<CheckIn> checkInQuery = checkInRepo.query().order("-id").offset(start).limit(limit);
		if(areaId != 0) {
			checkInQuery = checkInQuery.filter("area", areaRepo.getKey(businessKey, areaId));
		}
		
		checkInQuery.filter("status", CheckInStatus.COMPLETE);
	
		ArrayList<CheckInHistoryDTO> historyDTOList = new ArrayList<CheckInHistoryDTO>();
		
		for (CheckIn checkIn : checkInQuery) {
			Spot spot = spotRepo.getByKey(checkIn.getSpot());
			Bill bill = billRepo.getByProperty("checkIn", checkIn);
			historyDTOList.add(new CheckInHistoryDTO(checkIn, bill, spot));
		}
		
		return historyDTOList;
	}
}
