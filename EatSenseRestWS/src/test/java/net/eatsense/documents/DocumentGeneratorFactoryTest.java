package net.eatsense.documents;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import net.eatsense.documents.AbstractDocumentGenerator;
import net.eatsense.documents.CounterReportXLSGenerator;
import net.eatsense.documents.DocumentGeneratorFactory;
import net.eatsense.documents.SpotPurePDFGenerator;
import net.eatsense.domain.Document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Provider;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorFactoryTest {

	@Mock
	private Provider<CounterReportXLSGenerator> counterReportXLSGeneratorProvider;
	@Mock
	private Provider<SpotPurePDFGenerator> spotPurePDFGeneratorProvider;
	private DocumentGeneratorFactory ctrl;
	@Mock
	private SpotPurePDFGenerator spotPurePDFGenerator;
	@Mock
	private CounterReportXLSGenerator counterReportXLSGenerator;

	@Before
	public void setUp() throws Exception {
		when(spotPurePDFGeneratorProvider.get()).thenReturn(spotPurePDFGenerator);
		when(counterReportXLSGeneratorProvider.get()).thenReturn(counterReportXLSGenerator);
		ctrl = new DocumentGeneratorFactory(spotPurePDFGeneratorProvider, counterReportXLSGeneratorProvider);
	}

	@Test
	public void testCreateForDocumentSpotPurePDF() {
		Document document = new Document();
		document.setEntity("net.eatsense.domain.Spot");
		document.setRepresentation("pure");
		
		AbstractDocumentGenerator generator = ctrl.createForDocument(document );
		
		assertThat(generator, is(instanceOf(SpotPurePDFGenerator.class)));
	}

	
	@Test
	public void testCreateForDocumentCounterReportXLS() {
		Document document = new Document();
		document.setEntity("net.eatsense.counter.Counter");
		document.setRepresentation("report");
		
		AbstractDocumentGenerator generator = ctrl.createForDocument(document );
		
		assertThat(generator, is(instanceOf(CounterReportXLSGenerator.class)));
	}
}
