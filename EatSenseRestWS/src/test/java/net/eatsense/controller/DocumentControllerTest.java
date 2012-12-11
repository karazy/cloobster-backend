package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.eatsense.documents.SpotPurePDFGenerator;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.domain.embedded.DocumentStatus;
import net.eatsense.persistence.DocumentRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.DocumentDTO;
import net.eatsense.service.FileServiceHelper;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class DocumentControllerTest {
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
	private SpotPurePDFGenerator generator;

	@Before
	public void setUp() throws Exception {
		ctrl = new DocumentController(docRepo, validationHelper, spotPurePDFGeneratorProvider, spotRepoProvider, fileService);
		when(spotRepoProvider.get()).thenReturn(spotRepo);
		when(spotPurePDFGeneratorProvider.get()).thenReturn(generator);
	}

	/** 
	 * @return Test Data for Spot creation
	 */
	private DocumentDTO getTestDocData() {

		DocumentDTO data = new DocumentDTO();

		return data;
	}
	
	@Test
	public void testCreateDocument() throws Exception {
		Document document = new Document();
		when(docRepo.newEntity()).thenReturn(document );
		
		ctrl.createDocument(businessKey, getTestDocData());
		
		assertThat(document.getBusiness(), is(businessKey));
		assertThat(document.getCreateDate(), lessThan(new Date()));
		assertThat(document.getStatus(), is(DocumentStatus.PENDING));
		
		verify(docRepo).saveOrUpdate(document);
	}
}
