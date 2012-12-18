package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Location;
import net.eatsense.domain.TrashEntry;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;


public class LocationRepository extends GenericRepository<Location> {
	
	final static Class<Location> entityClass = Location.class;
	
	public LocationRepository() {
		super(Location.class);
	}
	
	public Location findByBarcode(String code) {
		Query<Spot> query = ofy().query(Spot.class).filter("barcode", code);		
		Spot bc = query.get();
		Location business = null;
		if(bc != null) {
			business = ofy().find(bc.getBusiness());
		}
			
		return business;
	}
	
	/**
	 * @param trashEntryKey
	 * @return
	 */
	public Location restoreLocation(Key<TrashEntry> trashEntryKey) {
		checkNotNull(trashEntryKey, "trashEntryKey was null");
		TrashEntry trashEntry = ofy().get(trashEntryKey);
		checkArgument(trashEntry.getEntityKey().getKind() == Key.getKind(Location.class), "Trashed entity not of type Business");
		
		@SuppressWarnings("unchecked")
		Location business = ofy().get((Key<Location>)trashEntry.getEntityKey());
		business.setTrash(false);
		
		saveOrUpdate(business);
		
		ofy().delete(trashEntryKey);
		
		return business;
	}
}
