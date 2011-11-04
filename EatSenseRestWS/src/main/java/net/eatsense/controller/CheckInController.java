package net.eatsense.controller;

import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckIn;

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

	@Inject
	public CheckInController(RestaurantRepository r, CheckInRepository checkInRepo) {
		this.restaurantRepo = r;
		this.checkInRepo = checkInRepo;
	}

	/**
	 * A users request to check in.
	 * 
	 * @param barcode
	 * @return
	 */
	public CheckIn checkInIntent(String barcode) {
		CheckIn checkIn = new CheckIn();
		if (barcode != null && barcode.length() > 0) {
			Restaurant restaurant = restaurantRepo.findByBarcode(barcode);
			if (restaurant != null) {
				checkIn.setRestaurantName(restaurant.getName());
				checkIn.setStatus("success");
				logger.info("CheckIn attempt with barcode {}", barcode);
			} else {
				logger.info("CheckIn attempt failed! Reason: invalid code");
				checkIn.setStatus("invalidCode");
			}
		} else {
			logger.info("CheckIn attempt failed! Reason: code missing");
			checkIn.setStatus("missingCode");
		}
		return checkIn;
	}

	/**
	 * A real check in
	 * 
	 * @param barcode
	 */
	public void checkIn(String barcode) {
		logger.info("CheckIn with barcode {}", barcode);
		CheckIn c = new CheckIn();
		checkInRepo.saveOrUpdate(c);
		//TODO load barcode an restaurant and save checkIn information
	}

}
