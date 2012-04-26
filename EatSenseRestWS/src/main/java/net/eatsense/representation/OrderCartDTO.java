package net.eatsense.representation;

import net.eatsense.domain.embedded.OrderStatus;

public class OrderCartDTO {
	OrderStatus status;

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}
}
