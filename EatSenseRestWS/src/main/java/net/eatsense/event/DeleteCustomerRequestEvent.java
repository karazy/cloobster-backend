package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;

public class DeleteCustomerRequestEvent extends CustomerRequestEvent {

	public DeleteCustomerRequestEvent(Business business, Request request) {
		super(business, request);
	}

}
