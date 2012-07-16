package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Company;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.AccountDTO;
import net.eatsense.representation.CockpitAccountDTO;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.ImageDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class CompanyResource {
	@Context
	ResourceContext resourceContext;
	
	@Context
	HttpServletRequest servletRequest;
	
	private final AccountController accountCtrl;
	
	private Company company;
	
	@Inject
	public CompanyResource(AccountController accountCtrl) {
		super();
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
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return accountCtrl.updateCompanyImage(account, company, updatedImage);
	}
	
	@PUT
	@RolesAllowed(Role.COMPANYOWNER)
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public CompanyDTO updateCompany(CompanyDTO companyData) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		if(account.getCompany().getId() == company.getId()) {
			return accountCtrl.updateCompany(company, companyData);
		}
		else
			throw new IllegalAccessException("account does not own the company to update");
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
	@RolesAllowed(Role.COMPANYOWNER)
	public List<AccountDTO> getAccounts(@QueryParam("role")String role) {
		return accountCtrl.getCompanyAccounts(company.getKey(), role);
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
	@RolesAllowed(Role.COMPANYOWNER)
	public AccountDTO createUserAccount(CockpitAccountDTO accountData) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return accountCtrl.createUserAccount(account, accountData);
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
	@RolesAllowed(Role.COMPANYOWNER)
	public AccountDTO updateUserAccount(@PathParam("id") long accountId, CockpitAccountDTO accountData) {
		Account ownerAccount = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return accountCtrl.updateUserAccount(accountCtrl.getAccount(accountId), ownerAccount, accountData);
	}
}
