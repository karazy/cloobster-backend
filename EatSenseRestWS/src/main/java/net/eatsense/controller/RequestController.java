package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.RequestDTO;

/**
 * Manages loading, creating and querying of requests from the repository.
 * @author Nils Weiher
 */
public class RequestController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final RequestRepository requestRepo;
	private final AreaRepository areaRepo;
	private final SpotRepository spotRepo;
	private final CheckInRepository checkInRepo;
	

	@Inject
	public RequestController(RequestRepository requestRepo, AreaRepository areaRepo, SpotRepository spotRepo, CheckInRepository checkInRepo) {
		super();
		this.areaRepo = areaRepo;
		this.spotRepo = spotRepo;
		this.checkInRepo = checkInRepo;
		this.requestRepo = requestRepo;
	}
	
	/**
	 * @param businessKey
	 * @param areaId
	 * @return
	 */
	public List<RequestDTO> getRequests(Key<Business> businessKey, long areaId, long spotId, long checkInId, Set<RequestType> types) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(types, "types was null");
		
		ArrayList<RequestDTO> requestDtos = new ArrayList<RequestDTO>();
		Query<Request> requests = requestRepo.query();
		if(areaId != 0) {
			requests = requests.filter("area", areaRepo.getKey(businessKey, areaId));
		}
		else if(spotId != 0){
			requests = requests.filter("spot", spotRepo.getKey(businessKey, spotId));
		}
		else if(checkInId != 0) {
			requests = requests.filter("checkIn", checkInRepo.getKey(businessKey, checkInId));
		}
		else {
			requests = requestRepo.query().ancestor(businessKey);
		}
		
		for (Request request : requests) {
			if(types.isEmpty() || types.contains(request.getType())) {
				requestDtos.add(new RequestDTO(request));
			}
		}
		
		return requestDtos;
	}
}
