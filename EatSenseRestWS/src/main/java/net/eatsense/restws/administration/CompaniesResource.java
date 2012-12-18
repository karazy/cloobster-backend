package net.eatsense.restws.administration;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.common.collect.Collections2;
import com.google.inject.Inject;

import net.eatsense.controller.CompanyController;
import net.eatsense.representation.CompanyDTO;

public class CompaniesResource {
	private final CompanyController ctrl;

	@Inject
	public CompaniesResource(CompanyController ctrl) {
		super();
		this.ctrl = ctrl;
	}
	
	@GET
	@Produces("application/json")
	public Collection<CompanyDTO> getAll() {
		return Collections2.transform(ctrl.getAll(), CompanyDTO.toDTO);
	}
	
	@GET
	@Path("{id}")
	@Produces("application/json")
	public CompanyDTO get(@PathParam("id")long id) {
		return new CompanyDTO(ctrl.get(id));
	}
}
