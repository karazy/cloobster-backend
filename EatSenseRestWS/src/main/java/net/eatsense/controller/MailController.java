package net.eatsense.controller;

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
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Inject;

public class MailController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Session session = Session.getDefaultInstance( new Properties(), null);
	private TemplateController templateCtrl;
	
	@Inject
	public MailController( TemplateController templateCtrl) {
		super();
		this.templateCtrl = templateCtrl;
	}

	public MimeMessage sendWelcomeMessage(UriInfo uriInfo, NewsletterRecipient recipient) throws MessagingException {
		MimeMessage mail = new MimeMessage(session);
		URI unsubscribeUri = uriInfo.getAbsolutePathBuilder().path("unsubscribe/{id}").queryParam("email", recipient.getEmail()).build(recipient.getId());
		
		mail.setFrom(new InternetAddress("reifschneider@karazy.net"));
		mail.setReplyTo(new Address[]{new InternetAddress("info@cloobster.com")});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmail()));
		String welcomeMail = templateCtrl.getAndReplace("newsletter-email-registered", unsubscribeUri.toString());
		
		// Treat first line as subject.
		int firstNewline = welcomeMail.indexOf("\n");
		String subject = welcomeMail.substring(0, firstNewline);
		String body = welcomeMail.substring(firstNewline);
		
		mail.setSubject(subject, "utf-8");

		logger.info("welcomeMail: {}", welcomeMail);
		mail.setText(body, "utf-8");
		
		Transport.send(mail);
		return mail;
	}
	
	public Message sendRegistrationConfirmation(UriInfo uriInfo, Account account) throws MessagingException {
		MimeMessage mail = new MimeMessage(session);
		URI confirmationUri = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/account/confirm/{token}").build(account.getEmailConfirmationHash());
		
		mail.setFrom(new InternetAddress("reifschneider@karazy.net"));
		mail.setReplyTo(new Address[]{new InternetAddress("info@cloobster.com")});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
				
		String confirmationText = templateCtrl.getAndReplace("account-confirm-email", account.getName(), confirmationUri.toString());
		
		int firstNewline = confirmationText.indexOf("\n");
		String subject = confirmationText.substring(0, firstNewline);
		String body = confirmationText.substring(firstNewline);
		
		mail.setSubject(subject, "utf-8");
		logger.info("confirmationText: {}", confirmationText);
		mail.setText(body, "utf-8");
		
		Transport.send(mail);
		return mail;
	}
	
	public Message sendAccountConfirmedMessage(Account account,Company company) throws MessagingException {
		MimeMessage mail = new MimeMessage(session);
				
		mail.setFrom(new InternetAddress("reifschneider@karazy.net"));
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
				
		String confirmationText = templateCtrl.getAndReplace("account-confirmed",
				account.getName(), account.getLogin(), account.getEmail(),
				Strings.nullToEmpty(account.getPhone()), company.getName());
		
		int firstNewline = confirmationText.indexOf("\n");
		String subject = confirmationText.substring(0, firstNewline);
		String body = confirmationText.substring(firstNewline);
		
		mail.setSubject(subject, "utf-8");
		logger.info("confirmationText: {}", confirmationText);
		mail.setText(body, "utf-8");
		
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
