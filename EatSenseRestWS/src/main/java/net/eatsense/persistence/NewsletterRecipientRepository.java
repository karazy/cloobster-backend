package net.eatsense.persistence;

import net.eatsense.domain.NewsletterRecipient;

public class NewsletterRecipientRepository extends GenericRepository<NewsletterRecipient> {
	static {
		GenericRepository.register(NewsletterRecipient.class);
	}
	public NewsletterRecipientRepository() {
		super(NewsletterRecipient.class);
	}
}
