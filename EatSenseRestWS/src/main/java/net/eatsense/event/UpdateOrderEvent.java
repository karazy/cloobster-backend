package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.embedded.CheckInStatus;

public class UpdateOrderEvent extends OrderEvent {
	
	private Optional<CheckInStatus> newCheckInStatus;
	private Optional<String> newSpotStatus;

	public UpdateOrderEvent(Location business, Order order, CheckIn checkIn) {
		super(business, order, checkIn);
		newCheckInStatus = Optional.absent();
		newSpotStatus = Optional.absent();
	}

	public Optional<CheckInStatus> getNewCheckInStatus() {
		return newCheckInStatus;
	}

	public void setNewCheckInStatus(CheckInStatus newCheckInStatus) {
		this.newCheckInStatus = Optional.fromNullable(newCheckInStatus);
	}

	public Optional<String> getNewSpotStatus() {
		return newSpotStatus;
	}

	public void setNewSpotStatus(String newSpotStatus) {
		this.newSpotStatus = Optional.fromNullable(newSpotStatus);
	}
}
