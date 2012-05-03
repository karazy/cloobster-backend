package net.eatsense.persistence;

import net.eatsense.domain.OrderChoice;

public class OrderChoiceRepository extends GenericRepository<OrderChoice> {
	static {
		GenericRepository.register(OrderChoice.class);
	}	
	public OrderChoiceRepository() {
		super(OrderChoice.class);
	}
}
