package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;


import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.FeedbackRepository;
import net.eatsense.representation.FeedbackDTO;
import net.eatsense.representation.FeedbackFormDTO;

public class FeedbackController {
	private final FeedbackFormRepository feedbackFormRepo;
	private final FeedbackRepository feedbackRepo;
	private final Validator validator;
	private final CheckInRepository checkInRepo;

	@Inject
	public FeedbackController(CheckInRepository checkInRepo, FeedbackFormRepository feedbackFormRepo, FeedbackRepository feedbackRepo, Validator validator) {
		super();
		this.checkInRepo = checkInRepo;
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
	public FeedbackFormDTO getFeedbackFormForBusiness(Location business) {
		checkNotNull(business, "business was null");
		if(business.getFeedbackForm() == null) {
			throw new net.eatsense.exceptions.NotFoundException("business has no feedback form");
		}
		FeedbackForm feedbackForm;
		try {
			feedbackForm = feedbackFormRepo.getByKey(business.getFeedbackForm());
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException("feedback form for business not found");
		}
		
		return new FeedbackFormDTO(feedbackForm);
	}

	/**
	 * Save a new Feedback entity, for the supplied business and checkin.
	 * 
	 * @param business
	 * @param checkIn
	 * @param feedbackData - Transfer object containing the data for the entity.
	 * @return Transfer object representing the new Feedback entity.
	 */
	public Feedback addFeedback(Location business, CheckIn checkIn,
			FeedbackDTO feedbackData) {
		checkNotNull(business, "business was null");
		checkNotNull(feedbackData, "feedbackData was null");
		checkArgument(feedbackData.getFormId() != 0, "feedbackData formId was zero");
		
		Feedback feedback = feedbackRepo.newEntity();
		
		validateAndUpdateFeedbackData(feedbackData, feedback);
		
		feedback.setBusiness(business.getKey());
		feedback.setCheckIn(checkIn.getKey());
		feedback.setForm(feedbackFormRepo.getKey(feedbackData.getFormId()));
	
		Key<Feedback> key = feedbackRepo.saveOrUpdate(feedback);
		// Save the Key with the CheckIn so that we can find it quicker.
		checkIn.setFeedback(key );
		checkInRepo.saveOrUpdate(checkIn);
		
		return feedback;
	}

	/**
	 * Load and update the Feedback entity of the given CheckIn with the supplied data.
	 * 
	 * @param checkIn
	 * @param feedbackData
	 * @return 
	 */
	public Feedback updateFeedback(CheckIn checkIn,	FeedbackDTO feedbackData) {
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(feedbackData, "feedbackData was null");
		
		if(checkIn.getFeedback() == null || !feedbackData.getId().equals(checkIn.getFeedback().getId())) {
			throw new net.eatsense.exceptions.NotFoundException("no feedback saved or unknown id");
		}
		
		Feedback feedback = feedbackRepo.getByKey(checkIn.getFeedback());
		
		validateAndUpdateFeedbackData(feedbackData, feedback);
		
		feedbackRepo.saveOrUpdate(feedback);
		
		return feedback;
	}

	/**
	 * Validate and set the data fields on the supplied Feedback entity.
	 * 
	 * @param feedbackData
	 * @param feedback
	 * @return The updated entity.
	 */
	public Feedback validateAndUpdateFeedbackData(FeedbackDTO feedbackData,
			Feedback feedback) {
		checkNotNull(feedback, "feedback was null");
		checkNotNull(feedbackData, "feedbackData object was null");
		Set<ConstraintViolation<FeedbackDTO>> violations = validator.validate(feedbackData);
		if(!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder("validation errors:");
			for (ConstraintViolation<FeedbackDTO> constraintViolation : violations) {
				sb.append(String.format(" \"%s\" %s.", constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
			}
			throw new ValidationException(sb.toString());
		}
		
		feedback.setAnswers(feedbackData.getAnswers());
		feedback.setComment(feedbackData.getComment());
		feedback.setDate(new Date());
		feedback.setEmail(feedbackData.getEmail());
		
		return feedback;
	}

	/**
	 * Retrieve the Feedback entity saved for the supplied CheckIn.
	 * 
	 * @param checkIn
	 * @param id
	 * @return  Transfer object representing the saved Feedback entity.
	 */
	public FeedbackDTO getFeedbackForCheckIn(CheckIn checkIn, long id) {
		checkNotNull(checkIn, "checkIn was null");
		if(checkIn.getFeedback() == null || checkIn.getFeedback().getId() != id)
			throw new net.eatsense.exceptions.NotFoundException("no feedback saved or unknown id");
		
		return new FeedbackDTO(feedbackRepo.getByKey(checkIn.getFeedback()));
	}
	
}
