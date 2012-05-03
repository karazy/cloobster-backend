package net.eatsense.restws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import net.eatsense.controller.AccountController;
import net.eatsense.representation.RecipientDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Path("/newsletter")
public class NewsletterResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private AccountController accountCtrl;

	@Inject
	public NewsletterResource(AccountController accountCtrl) {
		this.accountCtrl = accountCtrl;

	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	public void saveRecipient(RecipientDTO recipient) {
		accountCtrl.addNewsletterRecipient(recipient);
	}
	
	@Path("unsubscribe/{id}")
	@GET
	public String unsubscribe(@PathParam("id") long id, @QueryParam("email") String email) {
		accountCtrl.removeNewsletterRecipient(id, email);
		return "Sorry, that you wanted no more messages from the eatSense Team!";
	}
}
