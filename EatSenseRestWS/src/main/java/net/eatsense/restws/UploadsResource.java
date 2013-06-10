package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.Role;
import net.eatsense.controller.UploadController;
import net.eatsense.domain.Account;
import net.eatsense.representation.ImageCropDTO;
import net.eatsense.representation.ImageUploadDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Inject;

@Path("uploads")
public class UploadsResource {
	@Context
	HttpServletRequest servletRequest;
	@Context
	UriInfo uriInfo;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private UploadController uploadCtrl;
	
	@Inject	
	public UploadsResource(UploadController uploadCtrl) {
		super();
		this.uploadCtrl = uploadCtrl;
	}

	@GET
	@Path("imagesurl")
	@Produces("text/html")
	@RolesAllowed(Role.USER)
	public String getImagesUploadUrl() {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		String uploadUrl = "/uploads/images/new";
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
			
			uploadUrl = "/uploads/images/new";
		}
		return uploadCtrl.getUploadUrl(account, uploadUrl);
	}
	
	@POST
	@Path("images/new/{token}")
	@Produces("application/json")
	public Collection<ImageUploadDTO> handleUpload( @PathParam("token") String token, @FormParam("imageId") String imageId) {
		logger.info("uploads received for token: {}", token);
		return uploadCtrl.parseUploadRequest(token, servletRequest, imageId);
	}
	
	@PUT
	@Path("images/{blobKey}")
	@RolesAllowed(Role.USER)
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json")
	public ImageUploadDTO cropImage(@PathParam("blobKey") String blobKey, ImageCropDTO cropData) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		
		return uploadCtrl.cropUpload(account, blobKey, cropData);
	}
	
	@DELETE
	@Path("images/{blobKey}")
	@RolesAllowed(Role.USER)
	public void deleteUpload(@PathParam("blobKey") String blobKey) {
		Account account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		uploadCtrl.deleteUpload(account, blobKey);
	}
}
