package net.eatsense.persistence;

import net.eatsense.domain.OrderChoice;

public class OrderChoiceRepository extends GenericRepository<OrderChoice> {
	public OrderChoiceRepository() {
		super(OrderChoice.class);
	}
}
