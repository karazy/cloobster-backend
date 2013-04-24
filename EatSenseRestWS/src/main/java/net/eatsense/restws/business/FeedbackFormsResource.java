package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import net.eatsense.controller.FeedbackController;
import net.eatsense.domain.Business;
import net.eatsense.representation.FeedbackFormDTO;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

@Produces(MediaType.APPLICATION_JSON)
public class FeedbackFormsResource {
	private Business location;
	private final FeedbackController feedbackCtrl;

	@Inject
	public FeedbackFormsResource(FeedbackController feedbackCtrl) {
		this.feedbackCtrl = feedbackCtrl;
	}

	public Business getLocation() {
		return location;
	}

	public void setLocation(Business location) {
		this.location = location;
	}
	
	@GET
	public Iterable<FeedbackFormDTO> getFeedbackForms(@QueryParam("active") boolean getActive) {
		if(getActive) {
			return ImmutableList.of(feedbackCtrl.getActiveFeedbackFormForLocation(location));
		}
		else {
			return Iterables.transform(feedbackCtrl.getFeedbackFormsForLocation(location.getKey()), FeedbackFormDTO.toDTO);
		}
	}
}
