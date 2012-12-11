package net.eatsense.documents;

import net.eatsense.domain.Document;
import net.eatsense.domain.GenericEntity;

public abstract class AbstractDocumentGenerator<T extends GenericEntity<T>> {
	
	public abstract String getMimeType();
	
	public abstract byte[] generate(Iterable<T> entity, Document document);

}
