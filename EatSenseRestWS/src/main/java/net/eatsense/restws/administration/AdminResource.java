package net.eatsense.restws.administration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import net.eatsense.auth.AwesomeUserAuthorizer;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.ManagementUserDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.core.ResourceContext;

@Path("admin")
public class AdminResource {
	
	private final AwesomeUserAuthorizer auth;
	
	
	@Inject
	public AdminResource(AwesomeUserAuthorizer auth) {
		super();
		this.auth = auth;
	}

	@Context
	private ResourceContext resourceContext;
	
	@Context
	private SecurityContext securityContext;
	
	@Path("user")
	@GET
	@Produces("application/json")
	public ManagementUserDTO getAuthorizedUser() {
		String email = securityContext.getUserPrincipal().getName();
		return new ManagementUserDTO(email, auth.isAwesome(email));
	}
	
	@Path("s")
	public ServicesResource getServiceResource() {
		String email = securityContext.getUserPrincipal().getName();
		if(auth.isAwesome(email)) {
			return resourceContext.getResource(ServicesResource.class);
		}
		else {
			throw new IllegalAccessException(email + " is not an awesome user!");
		}
	}
	
	@Path("m")
	public ManagementResource getManagementResource() {
		return resourceContext.getResource(ManagementResource.class);
	}
}
