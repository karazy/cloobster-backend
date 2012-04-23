package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;

public class UpdateOrderEvent extends OrderEvent {
	
	private Optional<String> newCheckInStatus;
	private Optional<String> newSpotStatus;

	public UpdateOrderEvent(Business business, Order order, CheckIn checkIn) {
		super(business, order, checkIn);
		newCheckInStatus = Optional.absent();
		newSpotStatus = Optional.absent();
	}

	public Optional<String> getNewCheckInStatus() {
		return newCheckInStatus;
	}

	public void setNewCheckInStatus(String newCheckInStatus) {
		this.newCheckInStatus = Optional.fromNullable(newCheckInStatus);
	}

	public Optional<String> getNewSpotStatus() {
		return newSpotStatus;
	}

	public void setNewSpotStatus(String newSpotStatus) {
		this.newSpotStatus = Optional.fromNullable(newSpotStatus);
	}
}
