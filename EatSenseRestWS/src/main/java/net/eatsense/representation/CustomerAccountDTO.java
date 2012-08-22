package net.eatsense.representation;

import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

public class CustomerAccountDTO extends AccountDTO {
	private String checkInId;
	
	public CustomerAccountDTO() {
		super();
	}
	
	public CustomerAccountDTO(Account account) {
		super(account);
	}

	public CustomerAccountDTO(Account account, CheckIn checkIn) {
		super(account);
		if(checkIn != null) {
			checkInId = checkIn.getUserId();
		}
	}

	public String getCheckInId() {
		return checkInId;
	}

	public void setCheckInId(String checkInId) {
		this.checkInId = checkInId;
	}
}
