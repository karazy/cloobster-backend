package net.eatsense.persistence;

import java.util.Date;
import java.util.Iterator;

import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.collect.AbstractIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

public class BillRepository extends GenericRepository<Bill> {

	final static Class<Bill> entityClass = Bill.class;
	
	public BillRepository() {
		super(entityClass);
	}
	
	public Bill belongingToCheckInAndLocation(Business location, long checkInId) {
		return query().ancestor(location).filter("checkIn", CheckIn.getKey(checkInId)).get();
	}
	
	public Iterable<Bill> belongingToAreaAndCreatedInDateRange(final Business location,final Key<Area> areaKey,final  Date fromDate, final Date toDate) {
		Query<Bill> result = query().ancestor(location).filter("area", areaKey).order("creationTime").filter("creationTime >=", fromDate);
		final QueryResultIterator<Bill> resultIter = result.iterator();
		
		return new Iterable<Bill>() {
			
			@Override
			public Iterator<Bill> iterator() {
				return new AbstractIterator<Bill>() {
				    protected Bill computeNext() {
				        while (resultIter.hasNext()) {
				          Bill bill = resultIter.next();
				          if (bill.getCreationTime().after(toDate)) {
				        	  return endOfData();
				          }
				          else 
				        	  return bill;
				        }
				        return endOfData();
				      }
				    };
			}
		};
	}
}
