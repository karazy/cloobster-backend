package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.CheckIn;

public class MultiUpdateEvent extends CheckInEvent {
	
	private final int entityCount;
	private Optional<String> newCheckInStatus;
	private Optional<String> newSpotStatus;

	public MultiUpdateEvent(CheckIn checkIn, int entityCount) {
		super(checkIn);
		this.entityCount = entityCount;
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

	public int getEntityCount() {
		return entityCount;
	}
}
