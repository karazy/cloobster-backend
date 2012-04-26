package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.CheckIn;

public class UpdateCartEvent extends CheckInEvent {
	
	private Optional<String> newCheckInStatus;
	private Optional<String> newSpotStatus;

	public UpdateCartEvent(CheckIn checkIn) {
		super(checkIn);
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
