package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.documents.AbstractDocumentGenerator;
import net.eatsense.documents.CounterReportXLSGenerator;
import net.eatsense.documents.DocumentGeneratorFactory;
import net.eatsense.documents.SpotPurePDFGenerator;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.DocumentStatus;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.DocumentRepository;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.DocumentDTO;
import net.eatsense.service.FileServiceHelper;
import net.eatsense.validation.ValidationHelper;

import org.eclipse.jetty.http.AbstractGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

/**
 * 
 * @author Frederik Reifschneider
 */
public class DocumentController {
	
	private final DocumentRepository docRepo;
	private final ValidationHelper validator;
	private final FileServiceHelper fileService;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Provider<DocumentGeneratorFactory> generatorFactoryProvider;
	
	@Inject
	public DocumentController(DocumentRepository documentRepository,
			ValidationHelper validator, FileServiceHelper fileService, 
			Provider<DocumentGeneratorFactory> generatorFactoryProvider) {
		super();
		this.docRepo = documentRepository;
		this.validator = validator;
		this.fileService = fileService;
		this.generatorFactoryProvider = generatorFactoryProvider;
	}
	
	/**
	 * Create and save a new document.
	 * 
	 * @param businessKey
	 * 	ID of assigned business.
	 * @param docData
	 * 	Data for new document.
	 * @return
	 * 	Document object with generated id.
	 */
	public DocumentDTO createDocument(Key<Business> businessKey, DocumentDTO docData) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(docData, "docData was null");
		
		Document newDoc = docRepo.newEntity();
		newDoc.setBusiness(businessKey);
		newDoc.setCreateDate(new Date());
		newDoc.setStatus(DocumentStatus.PENDING);
		
		docData = update(newDoc, docData);
		
		return docData;
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
		checkNotNull(docData, "docData was null");
		
		validator.validate(docData);
		
		doc.setType(docData.getType());
		doc.setEntity(docData.getEntity());
		doc.setEntityIds(docData.getIds());
		doc.setEntityNames(docData.getNames());
		doc.setName(docData.getName());
		doc.setRepresentation(docData.getRepresentation());
		
		if(doc.isDirty()) {
			docRepo.saveOrUpdate(doc);
		}
		
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
		Document document;
		try {
			document = docRepo.getByKey(docKey);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException(String.format("No Document with id=%d found.", id));
		}
		
		if(document.getBlobKey() != null) {
			// Remove the document from the blobstore if there exists a blob.
			fileService.delete(document.getBlobKey());
		}

		docRepo.delete(docKey);
	}
	
	/**
	 * Get a single Document by Id
	 * 
	 * @param businessKey
	 * @param id
	 * @return Document entity saved with this id
	 */
	public Document get(Key<Business> businessKey, Long id) {
		try {
			return docRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException("No Document found with id="+id);
		}
	}
	
	/**
	 * @param document
	 * @return the mime type of the processed document
	 */
	public String getContentType(Document document) {
		if(document.getStatus() != DocumentStatus.COMPLETE) {
			throw new ServiceException("Can only retrieve mime type for completed Documents");
		}
		
		BlobInfoFactory blobInfoFact = new BlobInfoFactory();
		
		return blobInfoFact.loadBlobInfo(document.getBlobKey()).getContentType();
	}
	
	/**
	 * Generate a Document based on the saved meta data and entity type.
	 * 
	 * @param document
	 * @return
	 */
	public Document processAndSave(Document document) {
		checkNotNull(document, "document was null");
		
		if(document.getStatus() == DocumentStatus.COMPLETE) {
			logger.info("Document was already processed");
			return document;
		}
		
		
		AbstractDocumentGenerator generator = generatorFactoryProvider.get().createForDocument(document);

		if(generator == null) {
			logger.error("Unable to get generator for Document: entity={}, representation={}", document.getEntity(), document.getRepresentation());
			document.setStatus(DocumentStatus.ERROR);
			docRepo.saveOrUpdate(document);
			throw new ValidationException("Unkown entity name or representation in Document");
		}
				
		byte[] bytes = generator.generate(document);
		
		if(bytes != null) {
			BlobKey blobKey = fileService.saveNewBlob(document.getName(), generator.getMimeType(), bytes);
			
			document.setBlobKey(blobKey);
			document.setStatus(DocumentStatus.COMPLETE);
			docRepo.saveOrUpdate(document);
		}
		else {
			logger.error("No Document data generated for {}", document.getKey());
			throw new ServiceException("Document generation failed for Document");
		}
			
		return document;
	}
}
