package net.eatsense.persistence;

import net.eatsense.domain.Spot;
import net.eatsense.domain.Business;

import com.googlecode.objectify.Query;


public class BusinessRepository extends GenericRepository<Business> {
	
	final static Class<Business> entityClass = Business.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	
	public BusinessRepository() {
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
	
	
}
