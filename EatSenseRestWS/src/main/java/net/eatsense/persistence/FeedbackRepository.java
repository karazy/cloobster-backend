package net.eatsense.persistence;

import net.eatsense.domain.Feedback;

public class FeedbackRepository extends GenericRepository<Feedback> {
	public FeedbackRepository() {
		super(Feedback.class);
	}
}
