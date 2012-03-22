package net.eatsense.controller;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Request;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**
 * Manages restaurant data concerning the whole restaurant. 
 * 
 * @author Frederik Reifschneider
 *
 */
public class BusinessController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private CheckInRepository checkInRepo;
	private SpotRepository spotRepo;
	private RequestRepository requestRepo;
	
	@Inject
	public BusinessController(RequestRepository rr, CheckInRepository cr, SpotRepository sr) {
		this.requestRepo = rr;
		this.spotRepo = sr;
		this.checkInRepo = cr;
	}
	
	/**
	 * Retrieve initial status data of all spots for the given restaurantId
	 * (mainly used by the Eatsense Cockpit application).
	 * 
	 * @param restaurantId
	 * @return List of SpotCockpitDTO objects
	 */
	public List<SpotStatusDTO> getSpotStatusData(Long restaurantId){
		List<Spot> allSpots = spotRepo.getByParent(Restaurant.getKey(restaurantId));
		//Restaurant restaurant = restaurantRepo.getById(restaurantId);
		List<SpotStatusDTO> spotDtos = new ArrayList<SpotStatusDTO>();
		for (Spot spot : allSpots) {
			SpotStatusDTO spotDto = new SpotStatusDTO();
			spotDto.setId(spot.getId());
			spotDto.setName(spot.getName());
			spotDto.setGroupTag(spot.getGroupTag());
			spotDto.setCheckInCount(checkInRepo.ofy().query(CheckIn.class).filter("spot", spot.getKey()).filter("status !=", CheckInStatus.PAYMENT_REQUEST).count());
			Request request = requestRepo.ofy().query(Request.class).filter("spot",spot.getKey()).order("-receivedTime").get();
			
			if(request != null) {
				spotDto.setStatus(request.getStatus());
			}
			
			spotDtos.add(spotDto);
		}
		
		return spotDtos;
	}
}
