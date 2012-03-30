package net.eatsense.persistence;

import net.eatsense.domain.Spot;
import net.eatsense.domain.Business;

import com.googlecode.objectify.Query;


public class BusinessRepository extends GenericRepository<Business> {
	
	public BusinessRepository() {
		super();
		super.clazz = Business.class;
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
	
	
}
