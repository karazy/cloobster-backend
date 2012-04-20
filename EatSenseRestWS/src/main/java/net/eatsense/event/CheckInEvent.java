package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

import com.google.common.base.Optional;

public class CheckInEvent {

	protected CheckIn checkIn;
	protected Business business;
	private Optional<Integer> checkInCount;

	public CheckInEvent(CheckIn checkIn, Business business) {
		super();
		this.checkIn = checkIn;
		this.business = business;
		this.checkInCount = Optional.absent();
	}

	public Business getBusiness() {
		return business;
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