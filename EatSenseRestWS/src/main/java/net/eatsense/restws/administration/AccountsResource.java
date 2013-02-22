package net.eatsense.restws.administration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.auth.AccessToken;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.BusinessAccountDTO;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class AccountsResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AccountController accountCtrl;
	private Objectify ofy;

	@Inject
	public AccountsResource(AccountController accountCtrl, OfyService ofyService) {
		super();
		this.ofy = ofyService.ofy();
		this.accountCtrl = accountCtrl;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public Iterable<BusinessAccountDTO> getAccounts(@QueryParam("start") int start, @QueryParam("limit") int limit) {
		return Iterables.transform(ofy.query(Account.class).offset(start).limit(limit).fetch(), BusinessAccountDTO.toDTO);
	}
	
	@PUT
	@Path("{accountId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")	
	public BusinessAccountDTO updateAccount(@PathParam("accountId") long accountId, BusinessAccountDTO accountDto) {
		Account account = ofy.get(Account.class, accountId);
		
		account.setActive(accountDto.isActive());
		if(account.isDirty()) {
			ofy.put(account);
			if(!account.isActive()) {
				// Delete access tokens for account
				Key<Account> key = account.getKey();
				logger.info("Deactivated {}, deleting AccessTokens.", key);
				ofy.delete(ofy.query(AccessToken.class).filter("account", key));
			}
		}
		
		return new BusinessAccountDTO(account);
	}
}
