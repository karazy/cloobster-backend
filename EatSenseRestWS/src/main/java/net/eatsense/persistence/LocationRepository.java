package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Business;
import net.eatsense.domain.TrashEntry;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;


public class LocationRepository extends GenericRepository<Business> {
	
	final static Class<Business> entityClass = Business.class;
	
	public LocationRepository() {
		super(Business.class);
	}
	
	public Business findByBarcode(String code) {
		Query<Spot> query = ofy().query(Spot.class).filter("barcode", code);		
		Spot bc = query.get();
		Business business = null;
		if(bc != null) {
			business = ofy().find(bc.getBusiness());
		}
			
		return business;
	}
	
	/**
	 * @param trashEntryKey
	 * @return
	 */
	public Business restoreLocation(Key<TrashEntry> trashEntryKey) {
		checkNotNull(trashEntryKey, "trashEntryKey was null");
		TrashEntry trashEntry = ofy().get(trashEntryKey);
		checkArgument(trashEntry.getEntityKey().getKind() == Key.getKind(Business.class), "Trashed entity not of type Business");
		
		@SuppressWarnings("unchecked")
		Business business = ofy().get((Key<Business>)trashEntry.getEntityKey());
		business.setTrash(false);
		
		saveOrUpdate(business);
		
		ofy().delete(trashEntryKey);
		
		return business;
	}
}
