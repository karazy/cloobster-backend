package net.eatsense.controller;

import java.util.List;

import com.google.inject.Inject;

import net.eatsense.persistence.RequestRepository;
import net.eatsense.representation.RequestDTO;

/**
 * Manages loading, creating and querying of requests from the repository.
 * @author Nils Weiher
 */
public class RequestController {
	
	private final RequestRepository requestRepo;

	@Inject
	public RequestController(RequestRepository requestRepo) {
		super();
		this.requestRepo = requestRepo;
	}
	
	public List<RequestDTO> getRequestsForArea(long areaId) {
		return null;	
	}
}
