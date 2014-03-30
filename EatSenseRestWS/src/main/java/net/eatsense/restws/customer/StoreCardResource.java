package net.eatsense.restws.customer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.Account;
import net.eatsense.representation.StoreCardDTO;

public class StoreCardResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Account account;
	
	public StoreCardResource() {
		
	}
	
	
	@GET
	public List<StoreCardDTO> getStoreCards() {
		List<StoreCardDTO> cards = new ArrayList<StoreCardDTO>();
		
		return cards;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public StoreCardDTO createStoreCard(StoreCardDTO data) {
		//extract customerNumber and BisnessId
		
		return null;
	}
	
	
	
	

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}
	
	

}
