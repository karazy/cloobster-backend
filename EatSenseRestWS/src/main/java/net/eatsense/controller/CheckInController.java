package net.eatsense.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.eatsense.domain.Spot;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.BarcodeRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckInDTO;
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
	private BarcodeRepository barcodeRepo;

	@Inject
	public CheckInController(RestaurantRepository r, CheckInRepository checkInRepo, BarcodeRepository barcodeRepo) {
		this.restaurantRepo = r;
		this.checkInRepo = checkInRepo;
		this.barcodeRepo = barcodeRepo;
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
		if (barcode != null && barcode.length() > 0) {
			Restaurant restaurant = restaurantRepo.findByBarcode(barcode);
			Spot bc = barcodeRepo.getByProperty("barcode", barcode);
			if (restaurant != null) {
				logger.info("CheckIn attempt with barcode {}", barcode);
				String tmpUserId = IdHelper.generateId();
				String tmpNickName = NicknameGenerator.generateNickname();
				// set values for dto object
				checkInDto.setRestaurantName(restaurant.getName());
				checkInDto.setStatus("success");
				checkInDto.setUserId(tmpUserId);
				checkInDto.setSpot("Dummy Table");
				checkInDto.setNickname(tmpNickName);
				// set values for domain object
				checkIn.setRestaurant(restaurant.getKey());
				checkIn.setSpot(bc.getKey());
				checkIn.setUserId(tmpUserId);
				checkIn.setStatus(CheckInStatus.INTENT);
				checkInRepo.saveOrUpdate(checkIn);
			} else {
				logger.info("CheckIn attempt failed! Reason: invalid code");
				checkInDto.setStatus("invalidCode");
			}
		} else {
			logger.info("CheckIn attempt failed! Reason: code missing");
			checkInDto.setStatus("missingCode");
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
		// TODO validate params!
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);
		
		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("CheckIn with userId {}", userId);
			chkin.setStatus(CheckInStatus.CHECKEDIN);
			//TODO check nickname
			chkin.setNickname(checkIn.getNickname());
			checkInRepo.saveOrUpdate(chkin);
			// TODO only query with status != CheckInStatus.INTENT
			List<CheckIn> checkInsAtSpot = checkInRepo.getListByProperty("spot", chkin.getSpot());
			if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
				checkIn.setStatus(CheckInStatus.YOUARENOTALONE.toString());
			} else {
				checkIn.setStatus(CheckInStatus.CHECKEDIN.toString());
			}	
		}
		//TODO Error handling
		
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
	public Map<String, String> getUsersAtSpot(String userId) {
		Map<String, String> usersAtSpot = null;
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);
		if (chkin.getStatus() == CheckInStatus.CHECKEDIN) {
			List<CheckIn> checkInsAtSpot = checkInRepo.getListByProperty("spot", chkin.getSpot());
			if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
				usersAtSpot = new HashMap<String, String>();
				// Other users at this table exist.
				for (CheckIn checkIn : checkInsAtSpot) {
					if(!checkIn.getUserId().equals(userId)) {
						usersAtSpot.put(checkIn.getUserId(), checkIn.getNickname());
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
			if (checkInUser.getStatus() == CheckInStatus.CHECKEDIN && checkInLinkedUser.getStatus() != CheckInStatus.INTENT && checkInLinkedUser.getStatus() != CheckInStatus.PAYMENT_REQUEST) {
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

		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("Cancel CheckIn with userId {}", userId);
			checkInRepo.delete(chkin);
			
		} else {
			// Error handling
			
		}
	}

}
