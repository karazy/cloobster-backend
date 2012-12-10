package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.DocumentController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.DocumentDTO;

@Produces("application/json; charset=UTF-8")
public class DocumentsResource {
	
	private final DocumentController docCtrl;
	private Business business;
	private Account account;
	
	@Inject
	public DocumentsResource(DocumentController docCtrl) {
		super();
		this.docCtrl = docCtrl;
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
