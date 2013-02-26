package net.eatsense.documents;

import net.eatsense.domain.Document;

public class CounterReportXLSGenerator extends AbstractDocumentGenerator {

	@Override
	public String getMimeType() {
		return "application/vnd.ms-excel";
	}

	@Override
	public byte[] generate( Document document) {
		// TODO Auto-generated method stub
		return null;
	}

}
