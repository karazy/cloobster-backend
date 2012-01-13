package net.eatsense.controller;

import java.util.ArrayList;
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
import net.eatsense.util.IdHelper;
import net.eatsense.util.NicknameGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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

	@Inject
	public CheckInController(RestaurantRepository r, CheckInRepository checkInRepo, SpotRepository barcodeRepo) {
		this.restaurantRepo = r;
		this.checkInRepo = checkInRepo;
		this.barcodeRepo = barcodeRepo;
	}
	
	@Inject
    private Validator validator;

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

	/**
	 * Step 1 in checkIn process. User scanned barcode and and it is checked if
	 * a restaurant with this barcode exists.
	 * 
	 * @param barcode
	 * @return
	 */
	public CheckInDTO checkInIntent(String barcode) {
		CheckIn checkIn = new CheckIn();
		CheckInDTO checkInDto = new CheckInDTO();
		if (barcode != null && barcode.trim().length() > 0) {
			Restaurant restaurant = restaurantRepo.findByBarcode(barcode);
			Spot spot = barcodeRepo.getByProperty("barcode", barcode);
			if (restaurant != null) {
				logger.info("CheckIn attempt with barcode {}", barcode);
				String tmpUserId = IdHelper.generateId();
				String tmpNickName = NicknameGenerator.generateNickname();
				
				// set values for dto object
				checkInDto.setRestaurantName(restaurant.getName());
				checkInDto.setRestaurantId(restaurant.getId());
				checkInDto.setStatus(CheckInStatus.INTENT.toString());
				checkInDto.setUserId(tmpUserId);
				checkInDto.setSpot(spot.getName());
				checkInDto.setNickname(tmpNickName);
				
				// set values for domain object
				checkIn.setRestaurant(restaurant.getKey());
				checkIn.setSpot(spot.getKey());
				checkIn.setUserId(tmpUserId);
				checkIn.setStatus(CheckInStatus.INTENT);
				
				// validation 
				Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn);
				if( constraintViolations.isEmpty() )  {
					checkInRepo.saveOrUpdate(checkIn);
				}
				else {
					logger.info("checkInIntent(): CheckIn object validation failed. Message(s):");
					for (ConstraintViolation<CheckIn> violation : constraintViolations) {
						logger.info( violation.getMessage() );
					}
					checkInDto.setStatus(CheckInStatus.VALIDATION_ERROR.toString());
				}
			} else {
				logger.info("CheckIn attempt failed! Reason: " + barcode + " is not a valid code.");
				checkInDto.setStatus(CheckInStatus.BARCODE_ERROR.toString());
			}
		} else {
			logger.info("CheckIn attempt failed! Reason: no barcode provided.");
			checkInDto.setStatus(CheckInStatus.BARCODE_ERROR.toString());
		}
		return checkInDto;
	}

	/**
	 * Step 2 in CheckIn Process. An entry in checkIn database already exists
	 * with status {@link CheckInStatus#INTENT}.
	 * 
	 * @param userId
	 *            Id of user who tries to check in
	 * @param nickname
	 *            Either the pregenerated or a custom user nickname.
	 */
	public CheckInDTO checkIn(String userId, CheckInDTO checkIn) {
		if(userId == null || userId.isEmpty()) {
			logger.info("Error: userId is null or empty");
			checkIn.setStatus(CheckInStatus.ERROR.toString());
			
			return checkIn; 
		}
			
		logger.info("Searching for CheckIn with userId {}", userId);
		CheckIn chkinDatastore = checkInRepo.getByProperty("userId", userId);
		
		if (chkinDatastore != null && chkinDatastore.getStatus() == CheckInStatus.INTENT) {
			logger.info("CheckIn with userId {}", userId);			
						
			chkinDatastore.setNickname(checkIn.getNickname());
			
			// validation 
			Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(chkinDatastore, Default.class, CheckInStep2.class);
			if( !constraintViolations.isEmpty() )  {
				// constraint violations occurred setting status and logging error
				logger.info("CheckIn validation failed. Message(s):");
				for (ConstraintViolation<CheckIn> violation : constraintViolations) {
					logger.info( violation.getPropertyPath() + ": " +violation.getMessage() );
					if( violation.getPropertyPath().toString().equals("nickname") ) {
						
						checkIn.setStatus(CheckInStatus.VALIDATION_ERROR.toString());
						
						checkIn.setError( new ErrorDTO("checkInErrorNickname", "3", "25") ) ;
					}
					else {
						checkIn.setStatus(CheckInStatus.ERROR.toString() );
						return checkIn;
					}
				}
				
				return checkIn;
			}			
     					
			List<CheckIn> checkInsAtSpot = getOtherChekIns(chkinDatastore);
			Iterator<CheckIn> it = checkInsAtSpot.iterator();
			while(it.hasNext()) {
				CheckIn next = it.next();
				
				if(next.getNickname().equals(chkinDatastore.getNickname() ) ) {
					logger.info("Error: checkin with duplicate nickname tried: "+ chkinDatastore.getNickname());
					checkIn.setStatus(CheckInStatus.VALIDATION_ERROR.toString());
					checkIn.setError( new ErrorDTO("checkInErrorNicknameExists") ) ;
					//abort checkin
					return checkIn;
				}
				
				
			}
			
			while(it.hasNext()) {
				CheckIn next = it.next();
				
				if(next.getUserId().equals(userId) || !isPaymentLinkPossible(next)) {
					// filter this checkin because user is not available for linking
					it.remove();
				}
			}
			
			if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
				checkIn.setStatus(CheckInStatus.YOUARENOTALONE.toString());
			} else {
				checkIn.setStatus(CheckInStatus.CHECKEDIN.toString());
			}
			
			// save the checkin
			chkinDatastore.setStatus(CheckInStatus.CHECKEDIN);
			checkInRepo.saveOrUpdate(chkinDatastore);
			
		}
		else  {
			logger.info("Error: checkIn not found in datastore or status is not INTENT");
			checkIn.setStatus(CheckInStatus.ERROR.toString());
		}
		
		return checkIn;
	}

	/**
	 * Step 3 - I (optional) Shows a list of all checkedIn Users at the same
	 * spot.
	 * 
	 * @param userId
	 * @return Map<String,String> - key is another users id - value is another
	 *         users nickname If no other users at this spot exist
	 *         <code>null</code>.
	 */
	public List<User> getUsersAtSpot(String userId) {
		List<User> usersAtSpot = null;
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);
		
		if (chkin.getStatus() == CheckInStatus.CHECKEDIN) {
			
			List<CheckIn> checkInsAtSpot = getOtherChekIns(chkin);
			
			if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
				usersAtSpot = new ArrayList<User>();
				
				// Other users at this table exist.
				for (CheckIn checkIn : checkInsAtSpot) {
					
					if(!checkIn.getUserId().equals(userId) && isPaymentLinkPossible(checkIn)) {
						User user = new User();
						
						user.setUserId(checkIn.getUserId());
						user.setNickname(checkIn.getNickname());
						
						usersAtSpot.add(user);
					}
				}
			}
		}
		return usersAtSpot;
	}

	/**
	 * Step 3 - II (optional) If other users checkedIn at this table. User can
	 * choose if he wants to be linked with one of them. This is relevant for
	 * payment process.
	 * 
	 * @param userId
	 * @param linkedUserId
	 */
	public void linkToUser(String userId, String linkedUserId) {
		CheckIn checkInUser = checkInRepo.getByProperty("userId", userId);
		CheckIn checkInLinkedUser = checkInRepo.getByProperty("userId", linkedUserId);
		
		if(checkInUser != null && checkInLinkedUser != null) {
			if (checkInUser.getStatus() == CheckInStatus.CHECKEDIN && isPaymentLinkPossible(checkInLinkedUser)) {
				checkInUser.setLinkedUserId(linkedUserId);
				checkInRepo.saveOrUpdate(checkInUser);
			}
		}
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
	 * Return other checkings at the same spot.
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
				
				if(!checkIn.getUserId().equals(chkin.getUserId()) && checkIn.getStatus() != CheckInStatus.INTENT ) {
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
