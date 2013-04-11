package net.eatsense.persistence;

import net.eatsense.auth.AccessToken;
import net.eatsense.configuration.Configuration;
import net.eatsense.counter.Counter;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.Channel;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Company;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.domain.DashboardItem;
import net.eatsense.domain.Document;
import net.eatsense.domain.Feedback;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.InfoPage;
import net.eatsense.domain.Menu;
import net.eatsense.domain.NewsletterRecipient;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.translation.InfoPageT;
import net.eatsense.templates.Template;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	private final ObjectifyKeyFactory keyFactory;
	
	/**
	 * Try to register entity with Objectify and silently fail if already registered.
	 * 
	 * @param clazz
	 */
	public static void register(Class<?> clazz) {
		try {
			ObjectifyService.register(clazz);
		} catch (IllegalArgumentException e) {
			// We already registered the entity, okay to skip this.
		}
	}

	public static void registerEntities() {
		register(Account.class);
		register(AccessToken.class);
		register(Area.class);
		register(Bill.class);
		register(Channel.class);
		register(CheckIn.class);
		register(Choice.class);
		register(Company.class);
		register(Configuration.class);
		register(Counter.class);
		register(CustomerProfile.class);
		register(DashboardItem.class);
		register(Document.class);
		register(FeedbackForm.class);
		register(Feedback.class);
		register(InfoPage.class);
		register(InfoPageT.class);
		register(Business.class);
		register(Menu.class);
		register(NewsletterRecipient.class);
		register(NicknameAdjective.class);
		register(NicknameNoun.class);
		register(OrderChoice.class);
		register(Order.class);
		register(Product.class);
		register(Request.class);
		register(Subscription.class);
		register(Spot.class);
		register(Template.class);
	}
	
	@Inject
	public OfyService(ObjectifyKeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}
	
	public Objectify ofy() {
		return ObjectifyService.begin();
	}
	
	public ObjectifyKeyFactory keys() {
		return keyFactory;
	}
	
	public Objectify ofyTrans() {
		return ObjectifyService.beginTransaction();
	}
	
	public ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
