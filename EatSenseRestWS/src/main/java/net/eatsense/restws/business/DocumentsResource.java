package net.eatsense.restws.business;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.DocumentController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.representation.DocumentDTO;

@Produces("application/json; charset=UTF-8")
public class DocumentsResource {
	
	private final DocumentController docCtrl;
	private Business business;
	private Account account;
	private final BlobstoreService blobStoreService;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	public DocumentsResource(DocumentController docCtrl, BlobstoreService blobStoreService) {
		super();
		this.docCtrl = docCtrl;
		this.blobStoreService = blobStoreService;
	}
	
	/**
	 * Create a new document entity.
	 * @param data
	 * 		Document data.
	 * @return
	 */
	@POST
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public DocumentDTO createDocument(DocumentDTO data) {
		return docCtrl.createDocument(business.getKey(), data);
	}
	
	/**
	 * Retrieve all documents for given business
	 * @return
	 *  List of documents
	 */
	@GET
	public List<DocumentDTO> getAll() {
		return docCtrl.getAll(business.getKey());
	}
	
	/**
	 * Download the saved document from the blobstore.
	 * 
	 * @return
	 */
	@GET
	@Path("{id}/download")
	public Response getDownload(@Context HttpServletResponse response, @PathParam("id") Long id) {
		BlobKey blobKey = docCtrl.get(business.getKey(), id).getBlobKey();
		if(blobKey == null) {
			throw new NotFoundException("No Download available yet.");
		}
		
		try {
			blobStoreService.serve(blobKey, response);
		} catch (IOException e) {
			logger.error("IO error trying to serve blob={]", blobKey);
			throw new ServiceException("Internal error while trying to serve download.");
		}
		
		return Response.ok().build();
	}
	
	@POST
	@Path("{id}/generate")
	@RolesAllowed("admin")
	public Response generateDocument(@PathParam("id") Long id) {
		docCtrl.processAndSave(docCtrl.get(business.getKey(), id));
		
		return Response.ok().build();
	}
	
	/**
	 * Delete a single document.
	 * Deletes the entity as well as the actual file.
	 * @param id
	 * 	Document's id
	 */
	@DELETE
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteDocument(@PathParam("id") Long id) {
		docCtrl.delete(business.getKey(), id);
	}
	
	

	public Business getBusiness() {
		return business;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	
	
}
