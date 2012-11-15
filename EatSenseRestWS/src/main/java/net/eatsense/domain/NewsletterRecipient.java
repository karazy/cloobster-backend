package net.eatsense.domain;

import java.util.Date;

import com.googlecode.objectify.Key;

public class NewsletterRecipient extends GenericEntity<NewsletterRecipient> {
	@org.apache.bval.constraints.Email
	String email;
	Date entryDate;

	public NewsletterRecipient() {
		super();
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public Key<NewsletterRecipient> getKey() {
		return getKey(getId());
	}
	
	public static Key<NewsletterRecipient> getKey(long id) {
		return new Key<NewsletterRecipient>(NewsletterRecipient.class, id);
	}
 
}
