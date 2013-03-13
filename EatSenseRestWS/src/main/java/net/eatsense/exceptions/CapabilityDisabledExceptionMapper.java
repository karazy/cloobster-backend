package net.eatsense.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import net.eatsense.representation.ErrorDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;
import com.google.inject.Singleton;

@Provider
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class CapabilityDisabledExceptionMapper implements ExceptionMapper<CapabilityDisabledException> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Response toResponse(CapabilityDisabledException exception) {
		logger.error("Some AppEngine Service disabled. Returning error message", exception);
		return Response.status(Status.SERVICE_UNAVAILABLE).entity(new ErrorDTO("error.appengine", exception.getMessage())).build();
	}

}
