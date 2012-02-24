package net.eatsense.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.eatsense.controller.CheckInController;
import net.eatsense.representation.SpotDTO;

import com.google.inject.Inject;

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
}
