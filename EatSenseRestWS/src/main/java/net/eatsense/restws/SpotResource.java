package net.eatsense.restws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.CheckInController;
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
	
	@Context
	HttpServletRequest servletRequest;

	@Inject
	public SpotResource(CheckInController checkInCtr) {
		super();
		this.checkInCtr = checkInCtr;
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
	public SpotDTO getSpot(@PathParam("barcode") String barcode) {
		boolean checkInResume = servletRequest.getAttribute("net.eatsense.domain.CheckIn") != null;
		return checkInCtr.getSpotInformation(barcode, checkInResume );
	}
}
