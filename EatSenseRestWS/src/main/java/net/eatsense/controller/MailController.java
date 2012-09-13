package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.AccessToken.TokenType;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.auth.Role;
import net.eatsense.domain.Account;
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.NewAccountEvent;
import net.eatsense.event.NewCompanyAccountEvent;
import net.eatsense.event.NewNewsletterRecipientEvent;
import net.eatsense.event.ResetAccountPasswordEvent;
import net.eatsense.event.UpdateAccountEmailEvent;
import net.eatsense.event.UpdateAccountPasswordEvent;
import net.eatsense.persistence.CompanyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class MailController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Session session = Session.getDefaultInstance( new Properties(), null);
	private final TemplateController templateCtrl;
	private final AccessTokenRepository accessTokenRepo;
	private final CompanyRepository companyRepo;
	
	public static final String FROM_ADDRESS = "info@karazy.net";
	public static final String REPLY_TO_ADDRESS = "info@cloobster.com";
	
	@Inject
	public MailController( TemplateController templateCtrl, AccessTokenRepository accessTokenRepository, CompanyRepository companyRepo) {
		super();
		this.companyRepo = companyRepo;
		this.accessTokenRepo = accessTokenRepository;
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
		logger.info("send mail: {}", text);
		mail.setText(body, "utf-8");
	}
	
	public MimeMessage newMimeMessage() {
		return new MimeMessage(session); 
	}
	
	public MimeMessage sendMail(String emailTo, String text) throws AddressException, MessagingException {
		return sendMail(emailTo, text, REPLY_TO_ADDRESS);
	}
	
	public MimeMessage sendMail(String emailTo, String text, String replyTo) throws AddressException, MessagingException {
		checkArgument(!Strings.isNullOrEmpty(emailTo), "emailTo was null or empty");
		checkArgument(!Strings.isNullOrEmpty(replyTo), "replyTo was null or empty");
		
		if(Strings.isNullOrEmpty(text)) {
			logger.error("No template found, skipped sending mail to {}", emailTo);
			return null;
		}

		MimeMessage mail = new MimeMessage(session);
		
		mail.setFrom(new InternetAddress(FROM_ADDRESS));
		mail.setReplyTo(new Address[]{new InternetAddress(replyTo)});
		mail.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
		
		applyTextAndSubject(mail, text);
		
		Transport.send(mail);
		
		return mail;
	}

	public MimeMessage sendWelcomeMessage(String unsubscribeUrl, NewsletterRecipient recipient) throws MessagingException {
		String welcomeMail = templateCtrl.getAndReplace("newsletter-email-registered", unsubscribeUrl);
		

		return sendMail(recipient.getEmail(), welcomeMail);
	}
	
	@Subscribe
	public void sendWelcomeMessage(NewNewsletterRecipientEvent event) {
		NewsletterRecipient recipient = event.getRecipient();
		UriInfo uriInfo = event.getUriInfo();
		
		String unsubscribeUrl = uriInfo .getAbsolutePathBuilder().path("unsubscribe/{id}").queryParam("email", recipient .getEmail()).build(recipient.getId()).toString();
		try {
			sendWelcomeMessage(unsubscribeUrl, recipient);
		} catch (AddressException e) {
			logger.error("sending welcome mail failed", e);
		} catch (MessagingException e) {
			logger.error("sending welcome mail failed", e);
		}
	}
	
	public Message sendRegistrationConfirmation(String unsubcribeUrl, Account account) throws MessagingException {
		String confirmationText = templateCtrl.getAndReplace("account-confirm-email", account.getName(), unsubcribeUrl);
		
		// Send with "from" address set to welcome@cloobster.com.
		return sendMail(account.getEmail(), confirmationText, "welcome@cloobster.com");
	}
	
	@Subscribe
	public void sendRegistrationConfirmationMail(NewAccountEvent event) {
		Account account = event.getAccount();
		UriInfo uriInfo = event.getUriInfo();
		
		if(account.getRole().equals(Role.USER)) {
			sendCustomerAccountEmailConfirmation(account, uriInfo);
			return;
		}
		
		
		String accessToken = accessTokenRepo.create(TokenType.EMAIL_CONFIRMATION, account.getKey(), null).getToken();
		
		try {
			String unsubcribeUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/accounts/confirm/{token}").build(accessToken).toString();
			sendRegistrationConfirmation(unsubcribeUrl, account);
		} catch (AddressException e) {
			logger.error("sending confirmation mail failed", e);
		} catch (MessagingException e) {
			logger.error("sending confirmation mail failed", e);
		}
	}
	
	@Subscribe
	public void sendAccountSetupMail(NewCompanyAccountEvent event) {
		Account newAccount = event.getAccount();
		Account ownerAccount = event.getOwnerAccount();
		UriInfo uriInfo = event.getUriInfo();
		String accessToken = accessTokenRepo.create(TokenType.ACCOUNTSETUP, newAccount.getKey(), null).getToken();
		
		//TODO: Extract url strings as a config parameter.
		String setupUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/accounts/setup/{token}").build(accessToken).toString();
		
		try {
			sendAccountSetupMail(newAccount, ownerAccount, setupUrl);
		} catch (AddressException e) {
			logger.error("Error in e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error sending mail",e);
		}

	}
	
	@Subscribe
	public void sendAccountConfirmedMessage(ConfirmedAccountEvent event) {
		Account account = event.getAccount();
		Company company = companyRepo.getByKey(account.getCompany());
		
		try {
			sendAccountConfirmedMessage(account, company);
		} catch (MessagingException e) {
			logger.error("error sending confirmation notice", e);
		}
	}
	
	@Subscribe
	public void sendUpdateAccountEmailMessage(UpdateAccountEmailEvent event) {
		Account account = event.getAccount();
		UriInfo uriInfo = event.getUriInfo();
		String accessToken = accessTokenRepo.create(TokenType.EMAIL_CONFIRMATION, account.getKey(), null).getToken();
		String setupUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/accounts/confirm-email/{token}").build(accessToken).toString();
		
		String text = templateCtrl.getAndReplace("account-confirm-email-update", account.getName(), setupUrl);
		String textNotice = templateCtrl.getAndReplace("account-notice-email-update", account.getName(), account.getNewEmail());
		
		try {
			// Send e-mail asking for confirmation to new address.
			sendMail(account.getNewEmail(), text);
			// Send notice of e-mail update to old address.
			sendMail(account.getEmail(), textNotice);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendAccountPasswordUpdateNotification(UpdateAccountPasswordEvent event) {
		Account account = event.getAccount();
		String textNotice = templateCtrl.getAndReplace("account-notice-password-update", account.getName());
		
		try {
			// Send notice of password update to user.
			sendMail(account.getEmail(), textNotice);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	public Message sendAccountConfirmedMessage(Account account,Company company) throws MessagingException {
		String confirmationText = templateCtrl.getAndReplace("account-confirmed",
				account.getName(), account.getLogin(), account.getEmail(),
				Strings.nullToEmpty(account.getPhone()), company.getName());
		
		return sendMail("info@cloobster.com", confirmationText);
	}

	public MimeMessage sendAccountSetupMail(Account newAccount, Account ownerAccount, String setupUrl) throws AddressException, MessagingException {
		String text = templateCtrl.getAndReplace("account-setup-email", newAccount.getName(), ownerAccount.getName(), setupUrl);
			
		return sendMail(newAccount.getEmail(), text);
	}
	
	@Subscribe
	public void sendAcountPasswordResetMail(ResetAccountPasswordEvent event) {
		Account account = event.getAccount();
		UriInfo uriInfo = event.getUriInfo();
		//TODO: Add expiration time, and externalize to config file.
		String accessToken = accessTokenRepo.create(TokenType.PASSWORD_RESET, account.getKey(), null).getToken();
		String setupUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/accounts/reset-password/{token}").build(accessToken).toString();

		String text = templateCtrl.getAndReplace("account-forgotpassword-email", account.getName(), setupUrl);
		
		try {
			// Send e-mail with password reset link.
			sendMail(account.getEmail(), text);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	public void sendCustomerAccountEmailConfirmation(Account account, UriInfo uriInfo) {
		String accessToken = accessTokenRepo.create(TokenType.EMAIL_CONFIRMATION, account.getKey(), null).getToken();
		
		String confirmUrl = uriInfo.getBaseUriBuilder().path("/frontend").fragment("/accounts/confirm/{token}").build(accessToken).toString();
		
		String confirmationText = templateCtrl.getAndReplace("account-confirm-email", confirmUrl);
		
		try {
			// Send e-mail with password reset link.
			sendMail(account.getEmail(), confirmationText);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
}
