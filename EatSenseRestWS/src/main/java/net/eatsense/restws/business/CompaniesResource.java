package net.eatsense.restws.business;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.domain.Account;
import net.eatsense.domain.Company;
import net.eatsense.persistence.CompanyRepository;

import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

@Path("/b/companies")
public class CompaniesResource {
	@Context
	private ResourceContext resourceContext;
	@Context
	HttpServletRequest servletRequest;

	private CompanyRepository companyRepo;
	
	@Inject
	public CompaniesResource(CompanyRepository companyRepo) {
		super();
		this.companyRepo = companyRepo;
	}
	
	@Path("{id}")
	public CompanyResource getCompanyResource(@PathParam("id") Long id) {
		Company company;
		try {
			company = companyRepo.getById(id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		CompanyResource resource = resourceContext.getResource(CompanyResource.class);
		resource.setCompany(company);
		resource.setAccount(account);
		resource.setAuthorized(company.getKey().equals(account.getCompany()));
		return resource;
	}
}
