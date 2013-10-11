package net.eatsense.restws;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.LocationController;
import net.eatsense.representation.SpotDTO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

/**
 * Resource to retrieve spot information by a unique barcode.
 * 
 * @author Nils Weiher
 *
 */
@Path("/spots")
public class SpotResource {
	private CheckInController checkInCtr;
	
	@Context
	HttpServletRequest servletRequest;

	private final LocationController locationCtrl;

	@Inject
	public SpotResource(CheckInController checkInCtr, LocationController locationCtrl) {
		super();
		this.checkInCtr = checkInCtr;
		this.locationCtrl = locationCtrl;
	}
	
	@GET
	public String getSpot(@QueryParam("demo") boolean isDemo) {
		if(isDemo)
			return System.getProperty("net.karazy.spots.demo.barcode");
		else {
			throw new NotFoundException();
		}
	}
	
	/**
	 * Get the information of the spot identified by the given barcode.
	 * 
	 * @param barcode
	 * @return Spot as JSON 
	 */
	@GET
	@Path("{barcode}")
	@Produces("application/json; charset=UTF-8")
	public SpotDTO getSpot(@PathParam("barcode") String barcode,  @QueryParam("locationId") Long locationId) {
    //TODO Ugly way of retrieving welcome spot, refactor later
		if(locationId != null){	
			return locationCtrl.getWelcomeSpot(locationId);
		}

		boolean checkInResume = servletRequest.getAttribute("net.eatsense.domain.CheckIn") != null;
		return checkInCtr.getSpotInformation(barcode, checkInResume );
	}
}
