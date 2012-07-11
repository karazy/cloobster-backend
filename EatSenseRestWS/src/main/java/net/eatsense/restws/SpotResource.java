package net.eatsense.restws;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.eatsense.controller.CheckInController;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.SpotDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

/**
 * Resource to retrieve spot information by a unique barcode.
 * 
 * @author Nils Weiher
 *
 */
@Path("/spots")
public class SpotResource {
	private CheckInController checkInCtr;

	@Inject
	public SpotResource(CheckInController checkInCtr) {
		super();
		this.checkInCtr = checkInCtr;
	}
	
	/**
	 * Get the information of the spot identified by the given barcode.
	 * 
	 * @param barcode
	 * @return Spot as JsonObject 
	 */
	@GET
	@Path("{barcode}")
	@Produces("application/json; charset=UTF-8")
	public SpotDTO getSpot(@PathParam("barcode") String barcode) {
		return checkInCtr.getSpotInformation(barcode);
	}
	
	
	//TESTING PURPOSE
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<SpotDTO> getAllSpots() {
		
		SpotRepository sr = new SpotRepository();
		Collection<Spot> spots = sr.getAll();
		Collection<SpotDTO> dtos = new ArrayList<SpotDTO>();
		for (Spot s : spots) {
			
			dtos.add(checkInCtr.toSpotDto(s));
			
		}
		
	
		return dtos;
		
	}
}
