package net.eatsense.controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;

import net.eatsense.domain.NewsletterRecipient;

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
		
		mail.setFrom(new InternetAddress("weiher@karazy.net"));
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmail()));
		mail.setSubject("Thanks for subcribing to the eatSense newsletter.");
		
		String welcomeText = readWelcomeTextTemplate();
		welcomeText = welcomeText.replaceAll("\\{unsubscribeurl\\}", unsubscribeUri.toString());
		logger.info("welcomeText: {}", welcomeText);
		mail.setText(welcomeText);
		
		Transport.send(mail);
		return mail;
	}
	
	public String readWelcomeTextTemplate() {
		String welcomeText;
		try {
			welcomeText = CharStreams.toString(  new FileReader(new File("templates/welcomemail")));
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
