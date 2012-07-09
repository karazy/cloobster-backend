package net.eatsense.persistence;

import net.eatsense.domain.Order;

public class OrderRepository extends GenericRepository<Order> {
	public OrderRepository() {
		super(Order.class);
	}
}
