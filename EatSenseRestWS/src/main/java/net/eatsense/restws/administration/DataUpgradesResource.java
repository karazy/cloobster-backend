package net.eatsense.restws.administration;

import java.util.List;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.eatsense.controller.LocationController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.Business;
import net.eatsense.representation.DataUpgradesResultDTO;

public class DataUpgradesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final SubscriptionController subCtrl;
	private final LocationController locationCtrl;
	
	@Inject
	public DataUpgradesResource(SubscriptionController subCtrl,
			LocationController locationCtrl) {
		super();
		this.subCtrl = subCtrl;
		this.locationCtrl = locationCtrl;
	}
	
	@PUT
	@Path("subscriptionandwelcome")
	@Produces("application/json")
	public DataUpgradesResultDTO updateLocationSubscriptions() {
		List<Business> allLocations = locationCtrl.getLocations(0);
		int updateCount = 0;
		for (Business business : allLocations) {
			logger.info("Checking {} for incomplete data.", business.getKey());
			// Check for a welcome spot
			if(locationCtrl.getSpots(business.getKey(), 0, true).isEmpty()) {
				logger.info("Creating welcome area and spot ...");
				locationCtrl.createWelcomeAreaAndSpot(business.getKey());
				updateCount++;
			}
			
			if(business.getActiveSubscription() == null) {
				logger.info("Creating basic subscription ...");
				subCtrl.setBasicSubscription(business);
				updateCount++;
			}
		}
		
		return new DataUpgradesResultDTO("OK", updateCount);
	}
}
