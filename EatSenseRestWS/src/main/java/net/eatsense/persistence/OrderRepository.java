package net.eatsense.persistence;

import net.eatsense.domain.Order;

public class OrderRepository extends GenericRepository<Order> {
	static {
		GenericRepository.register(Order.class);
	}	
	public OrderRepository() {
		super(Order.class);
	}
}
