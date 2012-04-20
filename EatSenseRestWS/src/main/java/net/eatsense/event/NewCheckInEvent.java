package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Spot;

public class NewCheckInEvent {
	private CheckIn checkIn;
	private Business business;
	private Spot spot;
	private Optional<Integer> checkInCount;

	public Business getBusiness() {
		return business;
	}

	public NewCheckInEvent(Business business, Spot spot, CheckIn checkIn) {
		super();
		this.spot = spot;
		this.business = business;
		this.checkIn = checkIn;
	}

	public Spot getSpot() {
		return spot;
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
