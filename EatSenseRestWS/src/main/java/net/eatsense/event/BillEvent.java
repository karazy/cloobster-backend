package net.eatsense.event;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Location;
import net.eatsense.domain.CheckIn;

public class BillEvent {
	private Location business;
	private Bill bill;
	private CheckIn checkIn;
	
	public BillEvent(Location business, Bill bill, CheckIn checkIn) {
		super();
		this.business = business;
		this.bill = bill;
		this.checkIn = checkIn;
	}

	public Location getBusiness() {
		return business;
	}

	public Bill getBill() {
		return bill;
	}

	public CheckIn getCheckIn() {
		return checkIn;
	}
	
	
}
