package net.eatsense.representation;

import net.eatsense.domain.Account;
import net.eatsense.domain.CheckIn;

public class CustomerAccountDTO extends AccountDTO {
	private String checkInId;
	private long profileId;
	
	public CustomerAccountDTO() {
		super();
	}
	
	public CustomerAccountDTO(Account account) {
		super(account);
		if(account != null && account.getCustomerProfile() != null) {
			setProfileId(account.getCustomerProfile().getId());
		}
	}

	public CustomerAccountDTO(Account account, CheckIn checkIn) {
		this(account);
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

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}
}
