package net.eatsense.controller;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Request;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**
 * Manages data concerning one business. 
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
	 * Retrieve initial status data of all spots for the given business id.<br>
	 * (mainly used by the Eatsense Cockpit application).
	 * 
	 * @param businessId
	 * @return List of SpotCockpitDTO objects
	 */
	public List<SpotStatusDTO> getSpotStatusData(Long businessId){
		List<Spot> allSpots = spotRepo.getByParent(Business.getKey(businessId));
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
