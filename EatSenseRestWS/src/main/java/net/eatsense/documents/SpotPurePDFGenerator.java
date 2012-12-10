package net.eatsense.documents;

import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;

public class SpotPurePDFGenerator extends AbstractDocumentGenerator<Spot>{

	@Override
	public String getMimeType() {
		return "application/pdf"; 
	}

	@Override
	public byte[] generate(Spot entity, Document document) {
		// TODO Auto-generated method stub
		return null;
	}
}
