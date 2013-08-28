package net.eatsense.restws.administration;

import java.util.Collection;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jettison.json.JSONObject;

import com.google.common.collect.Collections2;
import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.CompanyController;
import net.eatsense.domain.Company;
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
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@Path("{id}/configurations/{name}")
	public Map<String, String> getConfiguration(@PathParam("id")long id, @PathParam("name") String name){
		Company company = ctrl.get(id); 
		return ctrl.getConfiguration(company, name);
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@Path("{id}/configurations/{name}")
	public Map<String, String> saveConfiguration(@PathParam("id")long id, @PathParam("name") String name, JSONObject configMap){
		Company company = ctrl.get(id); 
		return ctrl.saveConfiguration(company, name, configMap); 
	}
}
