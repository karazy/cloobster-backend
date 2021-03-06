package net.eatsense.representation;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;

import com.google.common.base.Function;
import com.googlecode.objectify.Key;

public class BusinessAccountDTO  extends AccountDTO {
	
	private Long companyId;
	private String phone;
	
	private List<Long> businessIds;
	
	public BusinessAccountDTO() {
		super();
		
		businessIds = new ArrayList<Long>();
	}
	
	/**
	 * Construct a new AccountDTO from the given Account entity.
	 * 
	 * @param account
	 */
	public BusinessAccountDTO(Account account) {
		super(account);
		if(account == null)
			return;
		
		this.phone = account.getPhone();
		
		businessIds = new ArrayList<Long>();

		if(account.getCompany() != null)
			this.companyId = account.getCompany().getId();
		
		if(account.getBusinesses()!=null) {
			for (Key<Business> businessKey : account.getBusinesses()) {
				businessIds.add(businessKey.getId());
			}
		}
	}
	
	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<Long> getBusinessIds() {
		return businessIds;
	}

	public void setBusinessIds(List<Long> businessIds) {
		this.businessIds = businessIds;
	}
	
	public final static Function<Account, BusinessAccountDTO> toDTO = 
		new Function<Account, BusinessAccountDTO>() {
			@Override
			public BusinessAccountDTO apply(Account input) {
				return new BusinessAccountDTO(input);
			}
	    };
}
