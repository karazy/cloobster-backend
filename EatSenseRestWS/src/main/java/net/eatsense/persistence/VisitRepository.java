package net.eatsense.persistence;

import net.eatsense.domain.Visit;

public class VisitRepository extends GenericRepository<Visit> {

	public VisitRepository(Class<Visit> clazz) {
		super(clazz);
	}

}
