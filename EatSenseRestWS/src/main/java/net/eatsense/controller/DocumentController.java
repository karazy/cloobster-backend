package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.domain.embedded.DocumentStatus;
import net.eatsense.persistence.DocumentRepository;
import net.eatsense.representation.DocumentDTO;
import net.eatsense.validation.ValidationHelper;

/**
 * 
 * @author Frederik Reifschneider
 */
public class DocumentController {
	
	private final DocumentRepository docRepo;
	private final ValidationHelper validator;
	
	@Inject
	public DocumentController(DocumentRepository documentRepository, ValidationHelper validator) {
		super();
		this.docRepo = documentRepository;
		this.validator = validator;
	}
	
	/**
	 * Create and save a new document.
	 * 
	 * @param businessKey
	 * 	ID of assigned business.
	 * @param doc
	 * 	Data for new document.
	 * @return
	 * 	Document object with generated id.
	 */
	public DocumentDTO createDocument(Key<Business> businessKey, DocumentDTO doc) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(doc, "doc was null");
		
		Document newDoc = docRepo.newEntity();
		newDoc.setBusiness(businessKey);
		
		//TODO FR Where to set createDate, status etc.? Directly on DTO or in update method or on doc entity?
		doc.setCreateDate(new Date());
		doc.setStatus(DocumentStatus.PENDING);
		
		return update(newDoc, doc);
	}
	
	/**
	 * Update a {@link Document} data
	 * @param doc
	 * 	Doc to update
	 * @param docData
	 * 	{@link DocumentDTO} with 
	 * @return
	 */
	public DocumentDTO update(Document doc, DocumentDTO docData) {
		checkNotNull(doc, "doc was null");
		
		
		validator.validate(docData);
		
		doc.setId(docData.getId());
		doc.setType(docData.getType());
		doc.setEntity(docData.getEntity());
		doc.setName(docData.getName());
		doc.setStatus(docData.getStatus());
		doc.setRepresentation(docData.getRepresentation());
		
		docRepo.saveOrUpdate(doc);
		
		return new DocumentDTO(doc);
	}
	
	/**
	 * Retrieve all documents for given business,
	 * @param businessKey
	 * 	Business for which to load documents.
	 * @return
	 * 	List of all documents.
	 */
	public List<DocumentDTO> getAll(Key<Business> businessKey) {
		checkNotNull(businessKey, "businessKey was null");
		List<Document> docs;
		
		docs = docRepo.getByParent(businessKey);
		
		ArrayList<DocumentDTO> docDTOs = new ArrayList<DocumentDTO>();
		
		for (Document document : docs) {
			docDTOs.add(new DocumentDTO(document));
		}
		
		return docDTOs;
	}
	
	/**
	 * Delete Document.
	 * 
	 * @param businessKey Parent key
	 * @param id for the Document entity to delete
	 */
	public void delete(Key<Business> businessKey, Long id) {
		checkNotNull(businessKey, "business was null");
		checkArgument(id != 0, "id was 0");
		
		Key<Document> docKey = docRepo.getKey(businessKey, id);

		docRepo.delete(docKey);
	}
}
