package net.eatsense.persistence;

import com.google.inject.Inject;
import com.googlecode.objectify.ObjectifyService;

import net.eatsense.domain.Area;

public class AreaRepository extends GenericRepository<Area> {

	@Inject
	public AreaRepository(ObjectifyService datastore) {
		super(datastore);		
	}

}
