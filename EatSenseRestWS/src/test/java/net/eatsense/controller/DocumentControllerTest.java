package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.eatsense.documents.DocumentGeneratorFactory;
import net.eatsense.documents.SpotPurePDFGenerator;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.DocumentStatus;
import net.eatsense.persistence.DocumentRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.DocumentDTO;
import net.eatsense.service.FileServiceHelper;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class DocumentControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	DocumentController ctrl;
	@Mock
	private DocumentRepository docRepo;
	@Mock
	private ValidationHelper validationHelper;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private Key<Business> businessKey;
	
	@Mock
	private Provider<SpotPurePDFGenerator> spotPurePDFGeneratorProvider;
	@Mock
	private Provider<SpotRepository> spotRepoProvider;
	@Mock
	private FileServiceHelper fileService;
	@Mock
	private SpotPurePDFGenerator spotPdfGenerator;

	@Mock
	private List<Key<Spot>> spotKeys;

	@Mock
	private Collection<Spot> spots;

	@Mock
	private Provider<DocumentGeneratorFactory> factoryProvider;

	@Mock
	private DocumentGeneratorFactory generatorFactory;

	@Before
	public void setUp() throws Exception {
		ctrl = new DocumentController(docRepo, validationHelper, fileService, factoryProvider);
		when(spotRepoProvider.get()).thenReturn(spotRepo);
		when(spotPurePDFGeneratorProvider.get()).thenReturn(spotPdfGenerator);
		when(factoryProvider.get()).thenReturn(generatorFactory);
	}

	/** 
	 * @return Test Data for Spot creation
	 */
	private DocumentDTO getTestDocData() {

		DocumentDTO data = new DocumentDTO();
		data.setCreateDate(new Date());
		data.setEntity("test");
		data.setIds(Arrays.asList(1l,2l,3l));
		data.setName("Test Document");
		data.setRepresentation("testrespresantion");
		data.setStatus(DocumentStatus.PENDING);
		data.setType("testtype");
		
		return data;
	}
	
	@Test
	public void testCreateDocument() throws Exception {
		Document document = new Document();
		when(docRepo.newEntity()).thenReturn(document );
		
		ctrl.createDocument(businessKey, getTestDocData());
		
		assertThat(document.getBusiness(), is(businessKey));
		assertThat(document.getCreateDate(), lessThanOrEqualTo(new Date()));
		assertThat(document.getStatus(), is(DocumentStatus.PENDING));
		
		verify(docRepo).saveOrUpdate(document);
	}
	
	@Test
	public void testUpdateDocumentType() throws Exception {
		Document document = new Document();
		DocumentDTO docData = getTestDocData();
		document.setCreateDate(docData.getCreateDate());
		document.setEntity(docData.getEntity());
		document.setEntityIds(docData.getIds());
		document.setName(docData.getName());
		document.setRepresentation(docData.getRepresentation());
		document.setStatus(docData.getStatus());
		document.setType("newtype");
		
		document.setDirty(false);
		
		ctrl.update(document, docData);
		
		verify(docRepo).saveOrUpdate(document);
		assertThat(document.getType(), is("testtype"));
	}
	
	@Test
	public void testUpdateDocumentEntity() throws Exception {
		Document document = new Document();
		DocumentDTO docData = getTestDocData();
		document.setCreateDate(docData.getCreateDate());
		document.setEntity("oldentity");
		document.setEntityIds(docData.getIds());
		document.setName(docData.getName());
		document.setRepresentation(docData.getRepresentation());
		document.setStatus(docData.getStatus());
		document.setType(docData.getType());
		
		document.setDirty(false);
		
		ctrl.update(document, docData);
		
		verify(docRepo).saveOrUpdate(document);
		assertThat(document.getEntity(), is(docData.getEntity()));
	}
	
	@Test
	public void testUpdateDocumentEntityIds() throws Exception {
		Document document = new Document();
		DocumentDTO docData = getTestDocData();
		document.setCreateDate(docData.getCreateDate());
		document.setEntity(docData.getEntity());
		document.setEntityIds(Arrays.asList(2l,3l,4l));
		document.setName(docData.getName());
		document.setRepresentation(docData.getRepresentation());
		document.setStatus(docData.getStatus());
		document.setType(docData.getType());
		
		document.setDirty(false);
		
		ctrl.update(document, docData);
		
		verify(docRepo).saveOrUpdate(document);
		assertThat(document.getEntityIds(), is(docData.getIds()));
	}
	
	@Test
	public void testUpdateDocumentName() throws Exception {
		Document document = new Document();
		DocumentDTO docData = getTestDocData();
		document.setCreateDate(docData.getCreateDate());
		document.setEntity(docData.getEntity());
		document.setEntityIds(docData.getIds());
		document.setName("Old Name");
		document.setRepresentation(docData.getRepresentation());
		document.setStatus(docData.getStatus());
		document.setType(docData.getType());
		
		document.setDirty(false);
		
		ctrl.update(document, docData);
		
		verify(docRepo).saveOrUpdate(document);
		assertThat(document.getName(), is(docData.getName()));
	}
	
	@Test
	public void testUpdateDocumentRepresantation() throws Exception {
		Document document = new Document();
		DocumentDTO docData = getTestDocData();
		document.setCreateDate(docData.getCreateDate());
		document.setEntity(docData.getEntity());
		document.setEntityIds(docData.getIds());
		document.setName(docData.getName());
		document.setRepresentation("oldrepresantation");
		document.setStatus(docData.getStatus());
		document.setType(docData.getType());
		
		document.setDirty(false);
		
		ctrl.update(document, docData);
		
		verify(docRepo).saveOrUpdate(document);
		assertThat(document.getRepresentation(), is(docData.getRepresentation()));
	}
	
	@Test
	public void testDeleteDocumentWithBlob() throws Exception {
		Long id = 1l;
		
		Document doc = mock(Document.class);
		
		@SuppressWarnings("unchecked")
		Key<Document> docKey = mock(Key.class);
		BlobKey blobKey = new BlobKey("Testkey");
		when(doc.getBlobKey()).thenReturn(blobKey );
		when(docRepo.getKey(businessKey, id)).thenReturn(docKey );
		when(docRepo.getByKey(docKey)).thenReturn(doc);
		
		ctrl.delete(businessKey, id );
		
		verify(fileService).delete(blobKey);
		verify(docRepo).delete(docKey);
	}
	
	@Test
	public void testDeleteDocumentWithoutBlob() throws Exception {
		Long id = 1l;
		
		Document doc = mock(Document.class);
		
		@SuppressWarnings("unchecked")
		Key<Document> docKey = mock(Key.class);
		when(docRepo.getKey(businessKey, id)).thenReturn(docKey );
		when(docRepo.getByKey(docKey)).thenReturn(doc);
		
		ctrl.delete(businessKey, id );
		
		verify(fileService, never()).delete((BlobKey) any());
		verify(docRepo).delete(docKey);
	}
	
	@Test
	public void testDeleteDocumentNotFound() throws Exception {
		Long id = 1l;
		
		Document doc = mock(Document.class);
		
		@SuppressWarnings("unchecked")
		Key<Document> docKey = mock(Key.class);
		BlobKey blobKey = new BlobKey("Testkey");
		when(doc.getBlobKey()).thenReturn(blobKey );
		when(docRepo.getKey(businessKey, id)).thenReturn(docKey );
		when(docRepo.getByKey(docKey)).thenThrow(NotFoundException.class);
		
		thrown.expect(net.eatsense.exceptions.NotFoundException.class);
		thrown.expectMessage("id="+id);
		
		ctrl.delete(businessKey, id );
	}
	
	@Test
	public void testProcessAndSaveDocumentSpotPurePDF() throws Exception {
		
		
		// mock Document
		Document doc = mock(Document.class);
		when(doc.getEntity()).thenReturn(Spot.class.getName());
		when(doc.getRepresentation()).thenReturn("pure");
		when(doc.getBusiness()).thenReturn(businessKey);
		String docName = "Test Document";
		when(doc.getName()).thenReturn(docName);
		List<Long> entityIds = Arrays.asList(1l,2l,3l);
		when(doc.getEntityIds()).thenReturn(entityIds );
		// mock Factory
		when(generatorFactory.createForDocument(doc)).thenReturn(spotPdfGenerator);
		
		// mock repository
		when(spotRepo.getKeys(businessKey, entityIds)).thenReturn(spotKeys );
		when(spotRepo.getByKeys(spotKeys)).thenReturn(spots);
		byte[] bytes = {127,127,127,0};
		
		when(spotPdfGenerator.generate(doc)).thenReturn(bytes );
		String mimeType = "mimeType";

		when(spotPdfGenerator.getMimeType()).thenReturn(mimeType);
		BlobKey blobKey = new BlobKey("TESTKEY");
		when(fileService.saveNewBlob(docName, mimeType, bytes)).thenReturn(blobKey );
		
		ctrl.processAndSave(doc );
		
		verify(doc).setBlobKey(blobKey);
		verify(doc).setStatus(DocumentStatus.COMPLETE);
		verify(docRepo).saveOrUpdate(doc);
	}
}
