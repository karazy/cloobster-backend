package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Company;
import net.eatsense.event.NewCompanyAccountEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.representation.BusinessAccountDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.ImageDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class CompanyResource {
	@Context
	ResourceContext resourceContext;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	HttpServletRequest servletRequest;
	
	private final AccountController accountCtrl;
	
	private Company company;

	private Account account;
	private boolean authorized;
	private final EventBus eventBus;
	
	@Inject
	public CompanyResource(AccountController accountCtrl, EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		this.accountCtrl = accountCtrl;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public CompanyDTO getCompany() {
		return accountCtrl.toCompanyDTO(company);
	}
	
	@POST
	@Path("images/{id}")
	@RolesAllowed(Role.COMPANYOWNER)
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ImageDTO updateOrCreateImage(@PathParam("id") String imageId, ImageDTO updatedImage) {
		if(!authorized) {
			throw new IllegalAccessException();
		}
		return accountCtrl.updateCompanyImage(account, company, updatedImage);
	}
	
	@PUT
	@RolesAllowed(Role.COMPANYOWNER)
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CompanyDTO updateCompany(CompanyDTO companyData) {
		if(!authorized) {
			throw new IllegalAccessException("account does not own the company to update");
		}
		
		return accountCtrl.updateCompany(company, companyData);	
	}
	
	/**
	 * Retrieve Accounts associated with the company.
	 * 
	 * @param role Only return Accounts with this role.
	 * @return List of Accounts,filtered by "role" if specified.
	 */
	@GET
	@Path("accounts")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public List<BusinessAccountDTO> getAccounts(@QueryParam("role")String role) {
		return accountCtrl.getCompanyAccounts(account, company.getKey(), role);
	}
	
	/**
	 * Create a new cockpit user account for the company from the specified data.
	 * 
	 * @param accountData "login", "password", and "businessIds" must be specified."name" is optional.
	 * @return New cockpit user account with the specified credentials.
	 */
	@POST
	@Path("accounts")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public BusinessAccountDTO createUserAccount(BusinessAccountDTO accountData, @Context UriInfo uriInfo) {
		
		if(Role.COCKPITUSER.equals(accountData.getRole())) {
			return accountCtrl.createCockpitUserAccount(account, accountData);
		}
		else if(Role.BUSINESSADMIN.equals(accountData.getRole())) {
			if(!account.getRole().equals(Role.COMPANYOWNER)) {
				throw new IllegalAccessException();
			}
			Account newAccount = accountCtrl.createOrAddAdminAccount(account, accountData);
			
			eventBus.post(new NewCompanyAccountEvent(newAccount, uriInfo, account));
			
			return new BusinessAccountDTO(newAccount);
		}
		else {
			throw new ValidationException("Invalid role specified: "+ accountData.getRole());
		}
	}
	
	/**
	 * Update a cockpit user account with new data.
	 * 
	 * @param accountId Numeric identifier of the account
	 * @param accountData Containing fields to update ("login", "password", "businessIds" and "name")
	 * @return Updated account data ("password" field omitted).
	 */
	@PUT
	@Path("accounts/{accountId}")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public BusinessAccountDTO updateUserAccount(@PathParam("accountId") long accountId, BusinessAccountDTO accountData) {
		if(!authorized) {
			throw new IllegalAccessException();
		}
		Account accountForCompany = accountCtrl.getAccountForCompany(accountId, company.getKey());
		if(account.getRole().equals(Role.BUSINESSADMIN) && !accountForCompany.getRole().equals(Role.COCKPITUSER)){
			throw new IllegalAccessException();
		}
		
		return accountCtrl.updateCompanyAccount(accountForCompany, account, accountData);
	}
	
	/**
	 * Delete an account from the company, also delete the account from the data store if it was a cockpituser.
	 * 
	 * @param accountId Id of the account to delete.
	 */
	@DELETE
	@Path("accounts/{accountId}")
	@RolesAllowed({Role.COMPANYOWNER, Role.BUSINESSADMIN})
	public void deleteUserAccount(@PathParam("accountId") long accountId) {
		if(!authorized) {
			throw new IllegalAccessException();
		}
		Account accountForCompany = accountCtrl.getAccountForCompany(accountId, company.getKey());
		
		if(account.getRole().equals(Role.BUSINESSADMIN) && !accountForCompany.getRole().equals(Role.COCKPITUSER)){
			throw new IllegalAccessException();
		}
		
		accountCtrl.deleteCompanyUserAccount(accountForCompany);
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}
}
