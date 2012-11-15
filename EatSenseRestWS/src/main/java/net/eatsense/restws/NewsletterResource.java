package net.eatsense.restws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.event.NewNewsletterRecipientEvent;
import net.eatsense.representation.RecipientDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

@Path("/newsletter")
public class NewsletterResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private AccountController accountCtrl;
	private EventBus eventBus;

	@Inject
	public NewsletterResource(AccountController accountCtrl, EventBus eventBus) {
		this.accountCtrl = accountCtrl;
		this.eventBus = eventBus; 
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	public void saveRecipient(RecipientDTO recipientData, @Context UriInfo uriInfo) {
		NewsletterRecipient recipient = accountCtrl.addNewsletterRecipient(recipientData);
		
		eventBus.post(new NewNewsletterRecipientEvent(recipient, uriInfo));
	}
	
	@Path("unsubscribe/{id}")
	@GET
	public String unsubscribe(@PathParam("id") long id, @QueryParam("email") String email) {
		accountCtrl.removeNewsletterRecipient(id, email);
		return "You will recieve no future messages to "+ email +" from eatSense.";
	}
}
