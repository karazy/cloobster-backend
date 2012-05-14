package net.eatsense.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.inject.Singleton;

import net.eatsense.representation.ErrorDTO;

@Provider
@Singleton
public class ServiceExceptionMapper implements
		ExceptionMapper<ServiceException> {

	@Override
	public Response toResponse(ServiceException arg0) {
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorDTO(arg0.getErrorKey(), arg0.getMessage(), arg0.getSubstitutions())).build();
	}
}
