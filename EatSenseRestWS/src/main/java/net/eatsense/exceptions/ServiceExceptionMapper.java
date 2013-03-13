package net.eatsense.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import net.eatsense.representation.ErrorDTO;

import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;
import com.google.inject.Singleton;

@Provider
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class ServiceExceptionMapper implements
		ExceptionMapper<ServiceException> {

	@Override
	public Response toResponse(ServiceException arg0) {
		ResponseBuilder builder;
		if(arg0 instanceof NotFoundException)
			builder = Response.status(Status.NOT_FOUND);
		else if(arg0 instanceof IllegalAccessException)
			builder = Response.status(Status.FORBIDDEN);
		else if(arg0 instanceof ValidationException)
			builder = Response.status(Status.BAD_REQUEST);
		else if(arg0 instanceof DataConflictException)
			builder = Response.status(Status.CONFLICT);
		else if(arg0 instanceof ReadOnlyException)
			builder = Response.status(405).header("Allow", "GET, HEAD, OPTIONS");
		else if(arg0 instanceof BillFailureException)
			// Send Unprocessable Entity HTTP error, for business logic failures.
			builder = Response.status(422);
		else if(arg0 instanceof ApiVersionException)
			builder = Response.status(460);
		else if(arg0 instanceof UnauthorizedException)
			builder = Response.status(Status.UNAUTHORIZED);
		else
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		
		return builder.entity(new ErrorDTO(arg0.getErrorKey(), arg0.getMessage(), arg0.getSubstitutions())).type(MediaType.APPLICATION_JSON).build();
	}
}
