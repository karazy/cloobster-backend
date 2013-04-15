package net.eatsense.persistence;

import java.util.Date;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

import net.eatsense.domain.Business;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;

public class FeedbackRepository extends GenericRepository<Feedback> {
	public FeedbackRepository() {
		super(Feedback.class);
	}
	
	/**
	 * @param location
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public Iterable<Feedback> belongingToFormLocationAndDateRange(Business location, Date fromDate, Date toDate, long formId) {
		Query<Feedback> query = query().ancestor(location).order("date");
		
		if(formId != 0) {
			query = query.filter("form", Key.create(FeedbackForm.class, formId));
		}
		
		if(fromDate != null) {
			query = query.filter("date >=", fromDate);
		}
		if(toDate != null) {
			query = query.filter("date <=", toDate);
		}
		logger.info("query={}", query);
		return query.fetch();
	}
}
