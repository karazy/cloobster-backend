package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.validation.CheckInStep2;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ErrorDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.util.IdHelper;
import net.eatsense.util.NicknameGenerator;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

/**
 * Controller for checkIn logic and process. When an attempt to checkIn at a
 * restaurant is made, various validations have must be executed.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInController {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private RestaurantRepository restaurantRepo;
	private CheckInRepository checkInRepo;
	private SpotRepository barcodeRepo;
	private NicknameGenerator nnGen;
	private Transformer transform;

	@Inject
	public CheckInController(RestaurantRepository r, CheckInRepository checkInRepo, SpotRepository barcodeRepo, NicknameGenerator nnGen, Transformer trans) {
		this.restaurantRepo = r;
		this.checkInRepo = checkInRepo;
		this.barcodeRepo = barcodeRepo;
		this.nnGen = nnGen;
		this.transform = trans;
	}
	
	@Inject
	private ObjectMapper mapper;
	
	
	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Inject
    private Validator validator;

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
    
    public SpotDTO getSpotInformation(String barcode) throws NotFoundException {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	SpotDTO spotDto = new SpotDTO();
    	Spot spot = barcodeRepo.getByProperty("barcode", barcode);
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
    	
    	Restaurant restaurant = restaurantRepo.getByKey(spot.getRestaurant());
    	
    	    	
    	spotDto.setBarcode(barcode);
    	spotDto.setName(spot.getName());
    	spotDto.setRestaurant(restaurant.getName());
    	spotDto.setRestaurantId(restaurant.getId());
    	spotDto.setPayments(restaurant.getPaymentMethods());
    	spotDto.setGroupTag(spot.getGroupTag());
    	
		return spotDto ;
    }

	public CheckInDTO createCheckIn(CheckInDTO checkInDto) {
		String message = null;
		
		if(checkInDto == null ) {
			throw new RuntimeException("checkin data is empty");
		}
		if( checkInDto.getStatus()==null || checkInDto.getStatus() != CheckInStatus.INTENT ) {
			throw new RuntimeException("checkin status should be INTENT but is " + checkInDto.getStatus());
		}
			
		// set values for domain object
		Spot spot = barcodeRepo.getByProperty("barcode", checkInDto.getSpotId());
		
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
    	
    	Restaurant restaurant = restaurantRepo.getByKey(spot.getRestaurant());
    	
    	CheckIn checkIn = new CheckIn();
    	
    	String checkInId = IdHelper.generateId();
		checkIn.setRestaurant(restaurant.getKey());
		checkIn.setSpot(spot.getKey());
		checkIn.setUserId(checkInId);
		checkIn.setStatus(CheckInStatus.CHECKEDIN);
		checkIn.setCheckInTime(new Date());
		checkIn.setDeviceId(checkInDto.getDeviceId());
		checkIn.setNickname(checkInDto.getNickname());

		// validation 
		Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn, Default.class, CheckInStep2.class);
		if( !constraintViolations.isEmpty() )  {
			// constraint violations occurred setting status and logging error
			logger.info("CheckIn validation failed. Message(s):");
			for (ConstraintViolation<CheckIn> violation : constraintViolations) {
				
				logger.info( violation.getPropertyPath() + ": " +violation.getMessage() );
				if(violation.getPropertyPath().toString().equals("nickname")) {
					
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInErrorNickname", "3","20"));
						logger.info("Writing json message:" + message);
					} catch (JsonGenerationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new RuntimeException(message);
				}
				else {
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInError", violation.getPropertyPath().toString() + " " + violation.getMessage()));
						logger.info("Writing json message:" + message);
					} catch (JsonGenerationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new RuntimeException(message);
				}
					
			}
			
			return null;
		}			
 		
		List<CheckIn> checkInsAtSpot = getOtherChekIns(checkIn);
		
		if(checkInsAtSpot != null) {
			Iterator<CheckIn> it = checkInsAtSpot.iterator();
			while(it.hasNext()) {
				CheckIn next = it.next();
				
				if(next.getNickname().equals(checkIn.getNickname() ) ) {
					logger.info("Error: checkin with duplicate nickname tried: "+ checkIn.getNickname());
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInErrorNicknameExists", ""));
					} catch (JsonGenerationException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//abort checkin
					throw new RuntimeException(message);
				}
				
				
			}
		}

		checkInRepo.saveOrUpdate(checkIn);
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);

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
		
		List<CheckIn> checkInsAtSpot = checkInRepo.getListByProperty("spot", spot.getKey());
		
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
		
		if(checkInUser.getLinkedUserId() != checkInDto.getLinkedCheckInId()) {
			CheckIn checkInLinkedUser = checkInRepo.getByProperty("userId", checkInDto.getLinkedCheckInId());
			if(checkInLinkedUser == null )
				throw new RuntimeException("linkedCheckInId unknown");
			
			if(checkInUser != null && checkInLinkedUser != null) {
				if (checkInUser.getStatus() == CheckInStatus.CHECKEDIN && isPaymentLinkPossible(checkInLinkedUser)) {
					checkInUser.setLinkedUserId(checkInLinkedUser.getUserId());
					save = true;
				}
				else
//					return "Can't link to this user";
				return null;
			}
		}
		//TODO: allow updating of other fields, like nickname

		if(save) {
			checkInRepo.saveOrUpdate(checkInUser);
		}
			
//		return "OK";
		return checkInDto;
	}

	/**
	 * User clicked cancel on checkIn confirm page. Deletes this checkIn form
	 * datastore.
	 * 
	 * @param userId
	 *            User issuing this request.
	 */
	public void cancelCheckIn(String userId) {
		//Don't return something. User is not really interested if check in cancel failed.
		//System has to deal with this.
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);
		
		if (chkin == null) { // CheckIn not found for this userId
			logger.info("Error: Recieved cancel for CheckIn with userId {}, but this userId was not found.", userId);
			return;
		}
			
		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("Cancel CheckIn with userId {}", userId);
			checkInRepo.delete(chkin);
			
		} else {
			// Error handling
			logger.info("Error: Recieved cancel for CheckIn with userId {}, but status was not INTENT.", userId);
			
		}
	}
	
	/**
	 * Load checkin.
	 * @param checkInId
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin otherwise <code>null</code>
	 */
	public CheckInDTO getCheckIn(String checkInId) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);		
		if(checkIn == null)
			throw new NotFoundException("unknown id");
		return transform.checkInToDto(checkIn);
	}
	
	/**
	 * Return other checkins at the same spot.
	 * 
	 * 
	 * @param chkin A user
	 * @return All users at spot checkedin.
	 */
	private List<CheckIn> getOtherChekIns(CheckIn chkin)
	{
		List<CheckIn> otherCheckIns = null;
		
		List<CheckIn> checkInsAtSpot = checkInRepo.getListByProperty("spot", chkin.getSpot());
		
		if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
			otherCheckIns = new ArrayList<CheckIn>();
			
			// Other users at this table exist.
			for (CheckIn checkIn : checkInsAtSpot) {
				
				if(!checkIn.getUserId().equals(chkin.getUserId()) && checkIn.getStatus() == CheckInStatus.CHECKEDIN ) {
					otherCheckIns.add(checkIn);
				}
			}
		}
		return otherCheckIns;
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
}
