package net.eatsense.documents;

import net.eatsense.counter.Counter;
import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Creates generator instances for a specific document
 * 
 * @author Nils Weiher
 *
 */
public class DocumentGeneratorFactory {
	private final Provider<SpotPurePDFGenerator> spotPurePDFGeneratorProvider;
	private final Provider<CounterReportXLSGenerator> counterReportXLSGenerator;
	
	
	@Inject
	public DocumentGeneratorFactory(
			Provider<SpotPurePDFGenerator> spotPurePDFGeneratorProvider,
			Provider<CounterReportXLSGenerator> counterReportXLSGenerator) {
		super();
		this.spotPurePDFGeneratorProvider = spotPurePDFGeneratorProvider;
		this.counterReportXLSGenerator = counterReportXLSGenerator;
	}

	public AbstractDocumentGenerator createForDocument(Document document) {
		AbstractDocumentGenerator generator = null;	
		
		if(document.getEntity().equals(Spot.class.getName()) && document.getRepresentation().equals("pure")) {
			generator =  spotPurePDFGeneratorProvider.get();
		}
		else if(document.getEntity().equals(Counter.class.getName()) && document.getRepresentation().equals("report")) {
			generator =  counterReportXLSGenerator.get(); 
		}
		return generator;
	}
}
