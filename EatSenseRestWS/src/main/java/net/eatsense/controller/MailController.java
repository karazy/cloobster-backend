package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import net.eatsense.auth.Role;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.domain.Order;
import net.eatsense.domain.Subscription;
import net.eatsense.event.ChannelOnlineCheckTimeOutEvent;
import net.eatsense.event.ConfirmedAccountEvent;
import net.eatsense.event.LocationCockpitsOfflineEvent;
import net.eatsense.event.NewAccountEvent;
import net.eatsense.event.NewCompanyAccountEvent;
import net.eatsense.event.NewNewsletterRecipientEvent;
import net.eatsense.event.NewPendingSubscription;
import net.eatsense.event.PlaceAllOrdersEvent;
import net.eatsense.event.ResetAccountPasswordEvent;
import net.eatsense.event.UpdateAccountEmailEvent;
import net.eatsense.event.UpdateAccountPasswordEvent;
import net.eatsense.persistence.CompanyRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.OfyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;

public class MailController {
	private static final String ACCOUNTS_CUSTOMER_CONFIRM = "/confirm/{token}";
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Session session = Session.getDefaultInstance( new Properties(), null);
	private final TemplateController templateCtrl;
	private final CompanyRepository companyRepo;
	private final AccountController accountCtrl;
	private String baseUri;
	
	public static final String FROM_ADDRESS = "info@karazy.net";
	public static final String REPLY_TO_ADDRESS = "info@cloobster.com";
	private Objectify ofy;
	
	@Inject
	public MailController( TemplateController templateCtrl, CompanyRepository companyRepo, AccountController accountCtrl, OfyService ofyService) {
		super();
		this.companyRepo = companyRepo;
		this.accountCtrl = accountCtrl;
		this.templateCtrl = templateCtrl;
		this.ofy = ofyService.ofy();
		
		// Get uri for email links from system property or use default if not set.
		this.baseUri = Objects.firstNonNull(Strings.emptyToNull(System.getProperty("net.karazy.url.outside")),
				"http://www.cloobster.com");
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
		
		logger.info("Sending mail to: {}, from: {}", emailTo, FROM_ADDRESS);
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
		
		UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri);
		String unsubscribeUrl = baseUriBuilder .path("unsubscribe/{id}").queryParam("email", recipient .getEmail()).build(recipient.getId()).toString();
		
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
		UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri);
		
		if(account.getRole().equals(Role.USER)) {
			sendCustomerAccountEmailConfirmation(account, uriInfo);
			return;
		}
		
		
		String accessToken = accountCtrl.createEmailConfirmationToken(account).getToken();
		
		try {
			String unsubcribeUrl = baseUriBuilder.fragment("/accounts/confirm/{token}").build(accessToken).toString();
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
		UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri);
		String accessToken = accountCtrl.createSetupAccountToken(newAccount).getToken();
		
		String setupUrl = baseUriBuilder.fragment("/accounts/setup/{token}").build(accessToken).toString();
		
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
		Optional<Company> company = Optional.absent();
		if(account.getCompany() != null) {
			try {
				company = Optional.of(companyRepo.getByKey(account.getCompany()));
			} catch (NotFoundException e1) {
				logger.error("Associated {} not found for Account({}).", account.getCompany(),account.getId());
			}
		}
		
		try {
			sendAccountConfirmedMessage(account, company);
		} catch (MessagingException e) {
			logger.error("error sending confirmation notice mail", e);
		}
	}
	
	@Subscribe
	public void sendUpdateAccountEmailMessage(UpdateAccountEmailEvent event) {
		Account account = event.getAccount();
		String accessToken = accountCtrl.createEmailConfirmationToken(account).getToken();
		UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		
		if(account.getRole().equals(Role.USER)) {
			uriBuilder = uriBuilder.path("/home/").fragment("/confirm-email/{token}");
		}
		else 
			uriBuilder = uriBuilder.fragment("/accounts/confirm-email/{token}");
		
		String setupUrl = uriBuilder.build(accessToken).toString();
		
		String name = Objects.firstNonNull(account.getName(), "Cloobster-Benutzer");
		
		String previousEmail = event.getPreviousEmail().or(account.getEmail());
		String newEmail = event.getPreviousEmail().isPresent() ? account.getEmail() : account.getNewEmail();
		
		String text = templateCtrl.getAndReplace("account-confirm-email-update", name, setupUrl);
		String textNotice = templateCtrl.getAndReplace("account-notice-email-update", name, newEmail);
		

		try {
			// Send e-mail asking for confirmation to new address.
			sendMail(newEmail, text);
			// Send notice of e-mail update to old address.
			sendMail(previousEmail, textNotice);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendAccountPasswordUpdateNotification(UpdateAccountPasswordEvent event) {
		Account account = event.getAccount();
		String name = Objects.firstNonNull(account.getName(), "Cloobster-Benutzer");
		String textNotice = templateCtrl.getAndReplace("account-notice-password-update", name);
		
		try {
			// Send notice of password update to user.
			sendMail(account.getEmail(), textNotice);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	public Message sendAccountConfirmedMessage(Account account, Optional<Company> company) throws MessagingException {
		String accountName = Strings.isNullOrEmpty(account.getName()) ? "(Kein Name)" : account.getName();
		String accountLogin = Objects.firstNonNull(account.getLogin(), "(Kein Login)");
		String accountInfo = account.getRole().equals(Role.USER) ? "Benutzerkonto" : "Firmenkonto";
		String companyName = company.isPresent() ? company.get().getName() : "(Kein Firmenaccount)";
		
		String confirmationText = templateCtrl.getAndReplace("account-confirmed",
				accountName, accountLogin, account.getEmail(),
				Strings.nullToEmpty(account.getPhone()), companyName, accountInfo);
		
		return sendMail("info@cloobster.com", confirmationText);
	}

	public MimeMessage sendAccountSetupMail(Account newAccount, Account ownerAccount, String setupUrl) throws AddressException, MessagingException {
		String text = templateCtrl.getAndReplace("account-setup-email", newAccount.getName(), ownerAccount.getName(), setupUrl);
			
		return sendMail(newAccount.getEmail(), text);
	}
	
	@Subscribe
	public void sendAcountPasswordResetMail(ResetAccountPasswordEvent event) {
		Account account = event.getAccount();
		//TODO: Add expiration time, and externalize to config file.
		String accessToken = accountCtrl.createPasswordResetToken(account).getToken();
		UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri);
		String setupUrl;
		String text;
		if(account.getRole().equals(Role.USER)) {
			setupUrl = baseUriBuilder.path("/home/").fragment("/reset-password/{token}").build(accessToken).toString();
			text = templateCtrl.getAndReplace("account-forgotpassword-email", account.getEmail(), setupUrl);
		}
		else {
			setupUrl = baseUriBuilder.fragment("/accounts/reset-password/{token}").build(accessToken).toString();
			text = templateCtrl.getAndReplace("account-forgotpassword-email", account.getEmail(), setupUrl);
		}
		
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
		String accessToken = accountCtrl.createEmailConfirmationToken(account).getToken();
		UriBuilder baseUriBuilder = UriBuilder.fromUri(baseUri);
		String confirmUrl = baseUriBuilder.path("/home/").fragment(ACCOUNTS_CUSTOMER_CONFIRM).build(accessToken).toString();
		
		String confirmationText = templateCtrl.getAndReplace("customer-account-confirm-email", confirmUrl);
		
		try {
			// Send e-mail with password reset link.
			sendMail(account.getEmail(), confirmationText);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendNewSubscriptionUpgradeRequestMail(NewPendingSubscription event) {
		Subscription newSubscription = event.getNewSubscription();
		Business location = event.getLocation();
		
		String companyName = "(no company)";
		if(location.getCompany() != null) {
			Company company = companyRepo.getByKey(location.getCompany());
			companyName = company.getName(); 
		}
		String adminUri = UriBuilder.fromUri(baseUri).path("admin/").fragment("/packages").build().toString();
		String mailText = templateCtrl.getAndReplace("location-upgrade-request", companyName, location.getName(), location.getEmail(), location.getPhone(), newSubscription.getName(), new Date().toString(), adminUri);
		
		try {
			// Send e-mail with password reset link.
			sendMail("info@cloobster.com", mailText);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendChannelOfflineAlertMail(ChannelOnlineCheckTimeOutEvent event) {
		Business location = ofy.get(event.getChannel().getBusiness());
		Account account = ofy.get(event.getChannel().getAccount());
		DateFormat df = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, Locale.GERMANY );
		
		String mailText = templateCtrl.getAndReplace("email-cockpit-offline-alert-de", location.getName(), df.format(event.getChannel().getLastOnlineCheck()));
		String mailAddress = account.getEmail();
		
		if(Strings.isNullOrEmpty(mailAddress)) {
			
			// find company owner account
			Account companyAccount = ofy.query(Account.class).filter("company", account.getCompany()).filter("role", Role.COMPANYOWNER).get();
			if(companyAccount != null) {
				mailAddress = companyAccount.getEmail();
			}
			else {
				logger.error("Unable to find COMPANYOWNER account for {}", account.getCompany());
			}
		}
		
		try {
			// Send e-mail with password reset link.
			sendMail(mailAddress, mailText);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendAllCockpitsOfflineEmail(LocationCockpitsOfflineEvent event) {
		Business location = ofy.get(event.getLocationKey());
		String mailText = templateCtrl.getAndReplace("email-cockpit-offline-alert-de", location.getName(), "unbekanntem Zeitpunkt");
		
		// find company owner account
		String mailAddress = null;
		Account companyAccount = ofy.query(Account.class).filter("company", location.getCompany()).filter("role", Role.COMPANYOWNER).get();
		if(companyAccount != null) {
			mailAddress = companyAccount.getEmail();
		}
		else {
			logger.error("Unable to sent E-Mail, could not find COMPANYOWNER account for {}", location.getCompany());
			return;
		}
	
		try {
			// Send e-mail with password reset link.
			sendMail(mailAddress, mailText);
		} catch (AddressException e) {
			logger.error("Error with e-mail address",e);
		} catch (MessagingException e) {
			logger.error("Error during e-mail sending", e);
		}
	}
	
	@Subscribe
	public void sendIncomingOrderNotificationEmail(PlaceAllOrdersEvent event) {
		// get location entity from event or business
		Business location = event.getOptBusiness().or(ofy.get(event.getCheckIn().getBusiness()));
		if(!location.isIncomingOrderNotifcationEnabled()) {
			logger.info("Incoming Order notification disabled, skipped sending.");
			return;
		}
		
		String receivingAdress = location.getEmail();
		if(Strings.isNullOrEmpty(receivingAdress)) {
			logger.warn("Unable to send Order notification, location has no email set.");
			return;
		}
		
		
		StringBuilder summaryBuilder = new StringBuilder();
		for (Order order : event.getOrders()) {
			summaryBuilder.append(String.format("\t%d - %s\n",order.getAmount(), order.getProductName()));
		}
		// Template parameter: orders count, area name, spot name, order summary text, cockpit url
		String mailText = templateCtrl.getAndReplace("order-placed-alert-de", String.valueOf(event.getEntityCount()));
	}
}
