package net.eatsense.event;

import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.NewsletterRecipient;

public class NewNewsletterRecipientEvent {
	private final NewsletterRecipient recipient;
	private final UriInfo uriInfo;
	
	public NewNewsletterRecipientEvent(NewsletterRecipient recipient, UriInfo uriInfo) {
		super();
		this.uriInfo = uriInfo;
		this.recipient = recipient;
	}

	public NewsletterRecipient getRecipient() {
		return recipient;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}
}
