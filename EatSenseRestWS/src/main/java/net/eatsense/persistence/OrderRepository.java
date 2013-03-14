package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.embedded.OrderStatus;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

public class OrderRepository extends GenericRepository<Order> {
	public OrderRepository() {
		super(Order.class);
	}
	
	public Query<Order> queryForCheckInAndStatus(CheckIn checkIn, OrderStatus... orderstatus) {
		checkNotNull(checkIn, "checkIn was null");
		
		Query<Order> query = query().filter("checkIn", checkIn);
		if(orderstatus.length > 0) {
			query.filter("status in", Arrays.asList(orderstatus));
		}
		
		return query;
	}
	
	public Iterable<Order> belongingToLocationAndCheckIn(Business location, Key<CheckIn> checkInKey) {
		return belongingToCheckIn(checkInKey);
	}

	public Iterable<Order> belongingToCheckIn(Key<CheckIn> checkInKey) {
		return query().filter("checkIn", checkInKey).fetch();
	}
}
