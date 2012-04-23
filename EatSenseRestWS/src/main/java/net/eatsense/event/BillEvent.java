package net.eatsense.event;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

public class BillEvent {
	private Business business;
	private Bill bill;
	private CheckIn checkIn;
	
	public BillEvent(Business business, Bill bill, CheckIn checkIn) {
		super();
		this.business = business;
		this.bill = bill;
		this.checkIn = checkIn;
	}

	public Business getBusiness() {
		return business;
	}

	public Bill getBill() {
		return bill;
	}

	public CheckIn getCheckIn() {
		return checkIn;
	}
	
	
}
