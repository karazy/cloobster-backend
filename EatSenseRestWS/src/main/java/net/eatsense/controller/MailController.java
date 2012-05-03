package net.eatsense.controller;

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
		
		mail.setText("Hello and welcome to the eatSense newsletter,\n" +
				"we will keep you informed about future developments and other events!\n\n" +
				"This is an automated message. If you did not register for the newsletter, unsubscribe here:\n" + unsubscribeUri.toString());
		logger.info("unsubcribe url: {}", unsubscribeUri.toString());
		
		Transport.send(mail);
		return mail;
	}
	
	public Message newMimeMessage() {
		return new MimeMessage(session); 
	}
	
	public void sendMail(Message message) throws MessagingException, SendFailedException, AddressException {
		Transport.send(message);
	}
}
