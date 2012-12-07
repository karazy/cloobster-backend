package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
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
		
		validator.validate(docData);
		
		doc.setId(docData.getId());
		doc.setType(docData.getType());
		doc.setEntity(docData.getEntity());
		doc.setName(docData.getName());
		doc.setStatus(docData.getStatus());
		doc.setCreateDate(docData.getCreateDate());
		
		docRepo.saveOrUpdate(doc);
		
		return new DocumentDTO(doc);
	}
	
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

}
