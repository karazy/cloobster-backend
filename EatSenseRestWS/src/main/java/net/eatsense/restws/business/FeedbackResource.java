package net.eatsense.restws.business;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import net.eatsense.auth.Role;
import net.eatsense.controller.FeedbackController;
import net.eatsense.domain.Business;
import net.eatsense.representation.FeedbackDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Produces(MediaType.APPLICATION_JSON)
public class FeedbackResource {
	private Business location;

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private final FeedbackController feedbackCtrl;
	
	@Inject
	public FeedbackResource(FeedbackController feedbackCtrl) {
		this.feedbackCtrl = feedbackCtrl;		
	}
	
	public void setLocation(Business location) {
		this.location = location;
	}
	
	@GET
	//@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public Iterable<FeedbackDTO> getFeedbackEntries(@QueryParam("formId") long formId, @QueryParam("fromDate") long fromTimestamp,@QueryParam("toDate") long toTimestamp) {
		Date fromDate = fromTimestamp != 0 ? new Date(fromTimestamp) : null;
		Date toDate = toTimestamp != 0 ? new Date(toTimestamp) : null;
		
		return feedbackCtrl.getFeedbackReport(location, formId, fromDate, toDate);
	}
}
