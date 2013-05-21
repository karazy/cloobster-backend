package net.eatsense.persistence;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Account;
import net.eatsense.domain.Visit;

public class VisitRepository extends GenericRepository<Visit> {

	public VisitRepository() {
		super(Visit.class);
	}

	public Iterable<Visit> belongingToAccountSortedByVisitAndCreationDate(Key<Account> account, int start, int limit) {
		logger.info("account={}, start={}",account, start);
		return ofy().query(Visit.class).ancestor(account).offset(start).limit(limit).order("-visitDate").order("-createdOn").fetch();
	}
}
