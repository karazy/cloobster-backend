package net.eatsense.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.inject.Singleton;

import net.eatsense.representation.ErrorDTO;

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
		else if(arg0 instanceof ReadOnlyException)
			builder = Response.status(405).header("Allow", "GET, HEAD, OPTIONS");
		else
			builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		
		return builder.entity(new ErrorDTO(arg0.getErrorKey(), arg0.getMessage(), arg0.getSubstitutions())).type(MediaType.APPLICATION_JSON).build();
	}
}
