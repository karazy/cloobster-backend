package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.domain.Business;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.representation.FeedbackFormDTO;

public class FeedbackController {
	private final FeedbackFormRepository feedbackFormRepo;

	@Inject
	public FeedbackController(FeedbackFormRepository feedbackFormRepo) {
		super();
		this.feedbackFormRepo = feedbackFormRepo;
	}
	
	public FeedbackFormDTO getFeedbackFormForBusiness(Business business) {
		checkNotNull(business, "business was null");
		FeedbackForm feedbackForm;
		try {
			feedbackForm = feedbackFormRepo.getByKey(business.getFeedbackForm());
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException("feedback form for business not found");
		}
		
		return new FeedbackFormDTO(feedbackForm);
	}
}
