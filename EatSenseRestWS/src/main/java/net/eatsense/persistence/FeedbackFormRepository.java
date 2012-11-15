package net.eatsense.persistence;

import net.eatsense.domain.FeedbackForm;

public class FeedbackFormRepository extends GenericRepository<FeedbackForm> {

	public FeedbackFormRepository() {
		super(FeedbackForm.class);
	}

}
