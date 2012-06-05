package net.eatsense.restws.business;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import net.eatsense.domain.Company;
import net.eatsense.persistence.CompanyRepository;

import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

@Path("/b/companies")
public class CompaniesResource {
	@Context
	private ResourceContext resourceContext;
	private CompanyRepository companyRepo;
	
	@Inject
	public CompaniesResource(CompanyRepository companyRepo) {
		super();
		this.companyRepo = companyRepo;
	}
	
	public CompanyResource getCompanyResource(@PathParam("id") Long id) {
		Company company;
		try {
			company = companyRepo.getById(id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		CompanyResource resource = resourceContext.getResource(CompanyResource.class);
		resource.setCompany(company);
		return resource;
	}
}
