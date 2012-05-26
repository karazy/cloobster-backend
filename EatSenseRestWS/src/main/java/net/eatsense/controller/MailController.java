package net.eatsense.controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.UriInfo;

import net.eatsense.domain.Account;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.representation.RegistrationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;

public class MailController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Session session = Session.getDefaultInstance( new Properties(), null);
	private UriInfo uriInfo;
	
	@Inject
	public MailController(UriInfo uriInfo) {
		super();
		this.uriInfo = uriInfo;
		// TODO Auto-generated constructor stub
	}

	public Message sendWelcomeMessage(NewsletterRecipient recipient) throws MessagingException {
		Message mail = new MimeMessage(session);
		URI unsubscribeUri = uriInfo.getAbsolutePathBuilder().path("unsubscribe/{id}").queryParam("email", recipient.getEmail()).build(recipient.getId());
		
		mail.setFrom(new InternetAddress("reifschneider@karazy.net"));
		mail.setReplyTo(new Address[]{new InternetAddress("info@cloobster.com")});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmail()));
		mail.setSubject("Thanks for subcribing to the eatSense newsletter.");
		
		String welcomeText = readWelcomeTextTemplate();
		welcomeText = welcomeText.replaceAll("\\{unsubscribeurl\\}", unsubscribeUri.toString());
		logger.info("welcomeText: {}", welcomeText);
		mail.setText(welcomeText);
		
		Transport.send(mail);
		return mail;
	}
	
	public Message sendRegistrationConfirmation(Account account) throws MessagingException {
		Message mail = new MimeMessage(session);
		URI confirmationUri = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/account/confirm/{token}").build(account.getEmailConfirmationHash());
		
		mail.setFrom(new InternetAddress("reifschneider@karazy.net"));
		mail.setReplyTo(new Address[]{new InternetAddress("info@cloobster.com")});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
		mail.setSubject("Cloobster service account confirmation.");
		
		String confirmationText = readEmailConfirmationTextTemplate();
		confirmationText = confirmationText.replaceAll("\\{confirmationurl\\}", confirmationUri.toString());
		logger.info("welcomeText: {}", confirmationText);
		mail.setText(confirmationText);
		
		Transport.send(mail);
		return mail;
	}
	
	public String readEmailConfirmationTextTemplate() {
		String welcomeText;
		try {
			welcomeText = CharStreams.toString(  new FileReader(new File("WEB-INF/templates/confirmationmail")));
		} catch (IOException e) {
			logger.error("error reading email confirmation template", e);
			welcomeText = "Welcome to the Cloobster service,\n" +
					"this is an automated message. Confirm your account with this link:\n" +
					"{confirmationurl}";
		}
		
		return welcomeText;
	}
	
	public String readWelcomeTextTemplate() {
		String welcomeText;
		try {
			welcomeText = CharStreams.toString(  new FileReader(new File("WEB-INF/templates/welcomemail")));
		} catch (IOException e) {
			logger.error("error reading welcome template", e);
			welcomeText = "Welcome to the cloobster Newsletter,\n" +
					"this is an automated message. If you did not register for the newsletter, unsubscribe here:\n" +
					"{unsubscribeurl}";
		}
		
		return welcomeText;
	}
	
	public Message newMimeMessage() {
		return new MimeMessage(session); 
	}
	
	public void sendMail(Message message) throws MessagingException, SendFailedException, AddressException {
		Transport.send(message);
	}
}
