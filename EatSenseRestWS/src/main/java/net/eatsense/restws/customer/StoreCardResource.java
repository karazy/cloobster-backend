package net.eatsense.restws.customer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.representation.StoreCardDTO;

/**
 * REST API for {@link StoreCardDTO} resources.
 * @author Frederik Reifschneider
 *
 */
public class StoreCardResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Account account;
	
	private AccountController accountCtrl;
	
	@Inject
	public StoreCardResource(AccountController accountController) {
		super();
		this.accountCtrl = accountController;
	}
	
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public List<StoreCardDTO> getStoreCards() {		
		return accountCtrl.getStoreCards(getAccount());
	}
	
	@GET
	@Path("{id}")
	@Produces("application/json; charset=UTF-8")
	public StoreCardDTO getStoreCard(@PathParam("id") Long id) {
		return new StoreCardDTO(accountCtrl.getStoreCard(id, account));
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public StoreCardDTO createStoreCard(StoreCardDTO data) {
		return accountCtrl.createStoreCard(getAccount(), data);
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public StoreCardDTO updateStoreCard(@PathParam("id") Long id, StoreCardDTO data) {
		return accountCtrl.updateStoreCard(id, getAccount(), data);
	}
	
	@DELETE
	@Path("{id}")
	public void deleteStoreCard(@PathParam("id") Long id) {
		accountCtrl.deleteStoreCard(id, getAccount());
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
