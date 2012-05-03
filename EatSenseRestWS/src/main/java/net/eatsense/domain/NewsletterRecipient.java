package net.eatsense.domain;

import java.util.Date;

public class NewsletterRecipient extends GenericEntity {
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
 
}
