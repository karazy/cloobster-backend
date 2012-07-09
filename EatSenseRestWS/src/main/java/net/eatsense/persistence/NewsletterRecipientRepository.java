package net.eatsense.persistence;

import net.eatsense.domain.NewsletterRecipient;

public class NewsletterRecipientRepository extends GenericRepository<NewsletterRecipient> {
	public NewsletterRecipientRepository() {
		super(NewsletterRecipient.class);
	}
}
