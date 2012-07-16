package net.eatsense.representation;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;

public class CockpitAccountDTO extends AccountDTO {
	/**
	 * Only supplied during account creation of a new cockpit user.
	 */
	private String password;
	private List<Long> businessIds;
	
	public CockpitAccountDTO() {
		super();
	}

	public CockpitAccountDTO(Account account) {
		super(account);
		
		if(account.getBusinesses()!=null) {
			businessIds = new ArrayList<Long>();
			for (Key<Business> businessKey : account.getBusinesses()) {
				businessIds.add(businessKey.getId());
			}
		}
	}

	public List<Long> getBusinessIds() {
		return businessIds;
	}

	public void setBusinessIds(List<Long> businessIds) {
		this.businessIds = businessIds;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
