package net.eatsense.persistence;

import net.eatsense.domain.Document;

public class DocumentRepository extends GenericRepository<Document> {

	public DocumentRepository() {
		super(Document.class);
	}

}
