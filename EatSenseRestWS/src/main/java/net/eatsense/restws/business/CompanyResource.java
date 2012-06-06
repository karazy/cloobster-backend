package net.eatsense.restws.business;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Company;
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
	public CompanyDTO getCompany() {
		return accountCtrl.toCompanyDTO(company);
	}
	
	@POST
	@Path("images/{id}")
	@RolesAllowed(Role.COMPANYOWNER)
	public ImageDTO updateOrCreateImage(@PathParam("id") String imageId, ImageDTO updatedImage) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return accountCtrl.updateCompanyImage(account, company, updatedImage);
	}
}
