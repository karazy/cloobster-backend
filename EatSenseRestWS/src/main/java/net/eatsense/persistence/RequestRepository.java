package net.eatsense.persistence;

import net.eatsense.domain.Request;

public class RequestRepository extends GenericRepository<Request> {
	public RequestRepository() {
		super(Request.class);
	}
}
