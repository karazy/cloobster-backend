package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;

import org.mortbay.jetty.MimeTypes;

import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.UploadController;
import net.eatsense.domain.Account;
import net.eatsense.representation.ImageUploadDTO;

@Path("uploads")
public class UploadsResource {
	@Context
	HttpServletRequest servletRequest;
	
	private UploadController uploadCtrl;
	
	@Inject	
	public UploadsResource(UploadController uploadCtrl) {
		super();
		this.uploadCtrl = uploadCtrl;
	}

	@GET
	@Path("images/url")
	@Produces("text/html")
	@RolesAllowed(Role.USER)
	String getImagesUploadUrl() {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		return uploadCtrl.getUploadUrl(account, "/uploads/images/new");
	}
	
	@POST
	@Path("images/new/{token}")
	@Produces("application/json")
	public Collection<ImageUploadDTO> handleUpload( @PathParam("token") String token) {
		
		return uploadCtrl.parseUploadRequest(servletRequest);
	}
}
