package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class NewBillEvent extends BillEvent {

	private Optional<String> newSpotStatus;

	public NewBillEvent(Business business, Bill bill, CheckIn checkIn) {
		super(business, bill, checkIn);
		this.newSpotStatus = Optional.absent();
	}
	
	public Optional<String> getNewSpotStatus() {
		return newSpotStatus;
	}

	public void setNewSpotStatus(String newSpotStatus) {
		this.newSpotStatus = Optional.fromNullable(newSpotStatus);
	}

}
