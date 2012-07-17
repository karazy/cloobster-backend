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
	
	public static final String FROM_ADDRESS = "reifschneider@karazy.net";
	public static final String REPLY_TO_ADDRESS = "info@cloobster.com";
	
	@Inject
	public MailController( TemplateController templateCtrl) {
		super();
		this.templateCtrl = templateCtrl;
	}
	
	/**
	 * @param mail
	 * @param text The first line will be treated as subject.
	 * @throws MessagingException
	 */
	private void applyTextAndSubject(MimeMessage mail, String text)
			throws MessagingException {
		int firstNewline = text.indexOf("\n");
		String subject = text.substring(0, firstNewline);
		String body = text.substring(firstNewline);
		
		mail.setSubject(subject, "utf-8");
		logger.info("mail text: {}", text);
		mail.setText(body, "utf-8");
	}
	
	public MimeMessage newMimeMessage() {
		return new MimeMessage(session); 
	}
	
	public MimeMessage sendMail(String emailTo, String text) throws AddressException, MessagingException {
		MimeMessage mail = new MimeMessage(session);
		
		mail.setFrom(new InternetAddress(FROM_ADDRESS));
		mail.setReplyTo(new Address[]{new InternetAddress(REPLY_TO_ADDRESS)});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
		
		applyTextAndSubject(mail, text);
		
		Transport.send(mail);
		return mail;
	}

	public MimeMessage sendWelcomeMessage(String unsubscribeUrl, NewsletterRecipient recipient) throws MessagingException {
		String welcomeMail = templateCtrl.getAndReplace("newsletter-email-registered", unsubscribeUrl);

		return sendMail(recipient.getEmail(), welcomeMail);
	}
	
	public Message sendRegistrationConfirmation(String unsubcribeUrl, Account account) throws MessagingException {
		String confirmationText = templateCtrl.getAndReplace("account-confirm-email", account.getName(), unsubcribeUrl);
		
		return sendMail(account.getEmail(), confirmationText);
	}
	
	public Message sendAccountConfirmedMessage(Account account,Company company) throws MessagingException {
		String confirmationText = templateCtrl.getAndReplace("account-confirmed",
				account.getName(), account.getLogin(), account.getEmail(),
				Strings.nullToEmpty(account.getPhone()), company.getName());
		
		return sendMail("info@cloobster.com", confirmationText);
	}

	public MimeMessage sendAccountSetupMail(Account account, Account ownerAccount, String setupUrl) throws AddressException, MessagingException {
		String text = templateCtrl.getAndReplace("account-setup-email", account.getName(), ownerAccount.getName(), setupUrl);
		return sendMail(account.getEmail(), text);
	}
}
