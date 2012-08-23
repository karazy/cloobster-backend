package net.eatsense.restws;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.common.base.Optional;

import net.eatsense.controller.CheckInController;
import net.eatsense.domain.Account;
import net.eatsense.representation.VisitDTO;

@Produces("application/json; charset=UTF-8")
@Path("/visits")
public class VisitsResource {

	private final CheckInController checkInCtrl;
	@Context
	private HttpServletRequest servletRequest;
	
	public VisitsResource(CheckInController checkInCtrl) {
		super();
		this.checkInCtrl = checkInCtrl;
	}
	
	/**
	 * Get previous visits for this account.
	 * 
	 * @param installId
	 * @return Array of visit objects, containing check in , bill and business data.
	 */
	@GET
	public List<VisitDTO> getVisits(@QueryParam("installId") String installId) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		
		return checkInCtrl.getVisits(Optional.fromNullable(account), installId );
	}
}
