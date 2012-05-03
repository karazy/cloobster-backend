package net.eatsense.persistence;

import net.eatsense.domain.Request;

public class RequestRepository extends GenericRepository<Request> {
	static {
		GenericRepository.register(Request.class);
	}	
	public RequestRepository() {
		super(Request.class);
	}

}
