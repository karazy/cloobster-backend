package net.eatsense.event;

import net.eatsense.domain.CheckIn;

public class CheckInActivityEvent extends CheckInEvent {

	private final boolean save;

	/**
	 * This event 
	 * 
	 * @param checkIn
	 * @param save
	 */
	public CheckInActivityEvent(CheckIn checkIn,boolean save) {
		super(checkIn);
		this.save = save;
	}

	public boolean isSave() {
		return save;
	}

}
