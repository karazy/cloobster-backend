package net.eatsense.documents;

import net.eatsense.domain.Document;
import net.eatsense.domain.GenericEntity;

public abstract class AbstractDocumentGenerator {
	
	public abstract String getMimeType();
	
	public abstract byte[] generate(Document document);

}
