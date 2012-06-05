package net.eatsense.restws.business;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Company;
import net.eatsense.representation.CompanyDTO;
import net.eatsense.representation.ImageDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

public class CompanyResource {
	@Context
	ResourceContext resourceContext;
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
	
	@PUT
	@Path("images/{id}")
	public ImageDTO updateOrCreateImage(@PathParam("id") String imageId, ImageDTO updatedImage) {
		return accountCtrl.updateCompanyImage(company, updatedImage);
	}
	
}
