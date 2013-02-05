package net.eatsense.restws.administration;

import java.util.List;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.controller.LocationController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.OfyService;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.DataUpgradesResultDTO;

public class DataUpgradesResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final SubscriptionController subCtrl;
	private final LocationController locationCtrl;

	private final AreaRepository areaRepo;

	private final SpotRepository spotRepo;
	
	@Inject
	public DataUpgradesResource(SubscriptionController subCtrl,
			LocationController locationCtrl, AreaRepository areaRepo, SpotRepository spotRepo) {
		super();
		this.subCtrl = subCtrl;
		this.locationCtrl = locationCtrl;
		this.areaRepo = areaRepo;
		this.spotRepo = spotRepo;
	}
	
	@PUT
	@Path("masterspots")
	@Produces("application/json")
	public DataUpgradesResultDTO updateAreasAddMasterSpot() {
		int updateCount = 0;
		
		QueryResultIterable<Area> areas = areaRepo.query().fetch();
		
		for (Area area : areas) {
			if(!area.isWelcome()) {
				boolean hasMasterSpot = false;
				Key<Area> areaKey = area.getKey();
				for (Spot spot : spotRepo.query().filter("area", areaKey)) {
					if(spot.isMaster()) {
						hasMasterSpot = true;
					}
				}
				if(!hasMasterSpot) {
					updateCount++;
					// No master Spot found, add it.
					
					locationCtrl.createMasterSpot(area.getBusiness(), areaKey);
				}				
			}
		}
		logger.info("Areas updated:  {}", updateCount);
		return new DataUpgradesResultDTO("OK", updateCount);
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
			if(locationCtrl.getSpots(business.getKey(), 0, true, false).isEmpty()) {
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
