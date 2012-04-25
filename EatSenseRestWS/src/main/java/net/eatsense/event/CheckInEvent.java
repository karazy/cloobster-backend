package net.eatsense.event;

import net.eatsense.domain.CheckIn;

import com.google.common.base.Optional;

public class CheckInEvent {

	protected CheckIn checkIn;
	private Optional<Integer> checkInCount;

	public CheckInEvent(CheckIn checkIn) {
		super();
		this.checkIn = checkIn;
		this.checkInCount = Optional.absent();
	}

	public CheckIn getCheckIn() {
		return checkIn;
	}

	public Optional<Integer> getCheckInCount() {
		return checkInCount;
	}

	public void setCheckInCount(Integer checkInCount) {
		this.checkInCount = Optional.fromNullable(checkInCount);
	}

}