package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.Request;

public class DeleteCustomerRequestEvent extends CustomerRequestEvent {

	private boolean fromCheckIn = false;

	public DeleteCustomerRequestEvent(Business business, Request request, boolean fromCheckIn) {
		super(business, request);
		
		this.setFromCheckIn(fromCheckIn);
	}

	public boolean isFromCheckIn() {
		return fromCheckIn;
	}

	public void setFromCheckIn(boolean fromCheckIn) {
		this.fromCheckIn = fromCheckIn;
	}

}
