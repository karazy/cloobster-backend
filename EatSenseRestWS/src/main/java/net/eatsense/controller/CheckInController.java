package net.eatsense.controller;

import net.eatsense.domain.Barcode;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.BarcodeRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.util.IdHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Controller for checkIn logic. When an attempt to checkIn at a restaurant is
 * made, various validations have must be executed.
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
			Barcode bc = barcodeRepo.getByProperty("barcode", barcode);
			if (restaurant != null) {
				logger.info("CheckIn attempt with barcode {}", barcode);
				String tmpUserId = IdHelper.generateId();
				// set values for dto object
				checkInDto.setRestaurantName(restaurant.getName());
				checkInDto.setStatus("success");
				checkInDto.setUserId(tmpUserId);
				checkInDto.setSpot("Dummy Table");
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
	 * Step 2 in CheckIn Process. An entry in checkIn database already existis
	 * with status {@link CheckInStatus#INTENT}.
	 * 
	 * @param userId
	 *            Id of user who tries to check in
	 */
	public String checkIn(String userId) {

		CheckIn chkin = checkInRepo.getByProperty("userId", userId);

		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("CheckIn with userId {}", userId);
			chkin.setStatus(CheckInStatus.CHECKEDIN);
			checkInRepo.saveOrUpdate(chkin);
			return "success";
		} else {
			// Error handling
			return "error";
		}
	}
	
	public String cancelCheckIn(String userId) {
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);

		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("Cancel CheckIn with userId {}", userId);
			checkInRepo.delete(chkin);
			return "success";
		} else {
			// Error handling
			return "error";
		}
	}

}
