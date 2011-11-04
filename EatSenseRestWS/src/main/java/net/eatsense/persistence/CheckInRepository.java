package net.eatsense.persistence;

import net.eatsense.representation.CheckIn;

import com.google.inject.Inject;
import com.googlecode.objectify.ObjectifyService;

public class CheckInRepository extends GenericRepository<CheckIn> {

	@Inject
	public CheckInRepository(ObjectifyService datastore) {
		super(datastore);
	}
	
}
