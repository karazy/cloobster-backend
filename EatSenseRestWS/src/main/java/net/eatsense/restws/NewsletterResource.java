package net.eatsense.restws;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.controller.AccountController;
import net.eatsense.controller.MailController;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.representation.RecipientDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Path("/newsletter")
public class NewsletterResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private AccountController accountCtrl;
	private MailController mailCtrl;

	@Inject
	public NewsletterResource(AccountController accountCtrl, MailController mailCtrl) {
		this.accountCtrl = accountCtrl;
		this.mailCtrl = mailCtrl;
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	public void saveRecipient(RecipientDTO recipientData) {
		NewsletterRecipient recipient = accountCtrl.addNewsletterRecipient(recipientData);
		try {
			mailCtrl.sendWelcomeMessage(recipient);
		} catch (AddressException e) {
			logger.error("sending welcome mail failed", e);
			if(recipient.getEmail().equals(e.getRef())) {
				throw new IllegalArgumentException("invalid email", e);
			}
		} catch (MessagingException e) {
			logger.error("sending welcome mail failed", e);
		}
	}
	
	@Path("unsubscribe/{id}")
	@GET
	public String unsubscribe(@PathParam("id") long id, @QueryParam("email") String email) {
		accountCtrl.removeNewsletterRecipient(id, email);
		return "You will recieve no future messages to "+ email +" from eatSense.";
	}
}
