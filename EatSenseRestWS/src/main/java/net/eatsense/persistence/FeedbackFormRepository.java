package net.eatsense.persistence;

import net.eatsense.domain.FeedbackForm;

public class FeedbackFormRepository extends GenericRepository<FeedbackForm> {
	
	static {
		GenericRepository.register(FeedbackForm.class);
	}

	public FeedbackFormRepository() {
		super(FeedbackForm.class);
	}

}
