package net.eatsense.persistence;

import net.eatsense.domain.Feedback;

public class FeedbackRepository extends GenericRepository<Feedback> {
	
	static {
		GenericRepository.register(Feedback.class);
	}
	
	public FeedbackRepository() {
		super(Feedback.class);
		// TODO Auto-generated constructor stub
	}
	
}
