package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;


import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.representation.FeedbackDTO;
import net.eatsense.representation.FeedbackFormDTO;

public class FeedbackController {
	private final FeedbackFormRepository feedbackFormRepo;
	private FeedbackRepository feedbackRepo;
	private Validator validator;

	@Inject
	public FeedbackController(FeedbackFormRepository feedbackFormRepo, FeedbackRepository feedbackRepo, Validator validator) {
		super();
		this.validator = validator;
		this.feedbackFormRepo = feedbackFormRepo;
		this.feedbackRepo = feedbackRepo;
	}
	
	/**
	 * Get the feedback form transfer object for the given Business entity.
	 * 
	 * @param business
	 * @return The feedback form dto.
	 */
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

	public FeedbackDTO addFeedback(Business business, CheckIn checkIn,
			FeedbackDTO feedbackData) {
		checkNotNull(business, "business was null");
		checkNotNull(feedbackData, "feedbackData was null");
		
		Set<ConstraintViolation<FeedbackDTO>> violations = validator.validate(feedbackData);
		if(!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder("validation errors:");
			for (ConstraintViolation<FeedbackDTO> constraintViolation : violations) {
				sb.append(String.format(" \"%s\" %s.", constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
			}
			throw new ValidationException(sb.toString());
		}
			
		
		Feedback feedback = new Feedback();
		
		feedback.setAnswers(feedbackData.getAnswers());
		feedback.setBusiness(business.getKey());
		if(checkIn != null) {
			feedback.setCheckIn(checkIn.getKey());
		}
		feedback.setComment(feedbackData.getComment());
		feedback.setDate(new Date());
		feedback.setEmail(feedbackData.getEmail());
		feedback.setForm(feedbackFormRepo.getKey(feedbackData.getFormId()));
		
		feedbackRepo.saveOrUpdate(feedback);
		
						
		return new FeedbackDTO(feedback);
	}
}
