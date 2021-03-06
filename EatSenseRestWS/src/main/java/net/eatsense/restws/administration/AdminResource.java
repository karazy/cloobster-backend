package net.eatsense.restws.administration;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

	private final ServletContext servletContext;
	
	
	@Inject
	public AdminResource(AwesomeUserAuthorizer auth, ServletContext servletContext) {
		super();
		this.auth = auth;
		this.servletContext = servletContext;
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
		return new ManagementUserDTO(email, auth.isAwesome(email), servletContext.getInitParameter("net.karazy.environment"));
	}
	
	@Path("s")
	public ServicesResource getServiceResource(@HeaderParam("X-AppEngine-TaskName") String appEngineTask) {
		if ((securityContext.getUserPrincipal() != null && auth
				.isAwesome(securityContext.getUserPrincipal().getName()))
				|| appEngineTask != null) {
			return resourceContext.getResource(ServicesResource.class);
		} else {
			throw new IllegalAccessException(securityContext.getUserPrincipal().getName() + " is not an awesome user!");
		}
	}
	
	@Path("m")
	public ManagementResource getManagementResource() {
		return resourceContext.getResource(ManagementResource.class);
	}
}
