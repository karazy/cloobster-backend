package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import com.googlecode.objectify.Query;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.embedded.OrderStatus;

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
}
