package net.eatsense.event;

import com.google.common.base.Optional;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;

public class NewBillEvent extends BillEvent {

	private Optional<String> newSpotStatus;
	private final boolean fromBusiness;
	

	public NewBillEvent(Location business, Bill bill, CheckIn checkIn, boolean fromBusiness) {
		super(business, bill, checkIn);
		this.newSpotStatus = Optional.absent();
		this.fromBusiness = fromBusiness;
	}
	
	public Optional<String> getNewSpotStatus() {
		return newSpotStatus;
	}

	public void setNewSpotStatus(String newSpotStatus) {
		this.newSpotStatus = Optional.fromNullable(newSpotStatus);
	}

	public boolean isFromBusiness() {
		return fromBusiness;
	}
}
