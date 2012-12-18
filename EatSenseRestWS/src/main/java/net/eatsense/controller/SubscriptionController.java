package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import net.eatsense.domain.Business;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

public class SubscriptionController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Objectify ofy;
	private final ValidationHelper validator;

	@Inject
	public SubscriptionController(OfyService ofy,  ValidationHelper validator) {
		super();
		
		this.ofy = ofy.ofy();
		this.validator = validator;
	}
	
	public Subscription createAndSavePackage(SubscriptionDTO subscriptionData) {
		Subscription subscription = new Subscription();
		subscription.setTemplate(true);
		subscriptionData.setStatus(SubscriptionStatus.PENDING);
		
		return update(subscription, subscriptionData);	
	}
	
	/**
	 * @param template return only Subscription entities flagged as template
	 * @return All saved Subscription entities 
	 */
	public Iterable<Subscription> getAll(boolean template) {
		return ofy.query(Subscription.class).filter("template", template).fetch();
	}
	
	/**
	 * @param businessId
	 * @return All Subscriptions for this entity.
	 */
	public Iterable<Subscription> get(long businessId) {
		return ofy.query(Subscription.class).filter("business", Business.getKey(businessId));
	}
	
	/**
	 * @param name
	 * @return Subscription entity saved with that name
	 */
	public Subscription getPackage(long	id) throws NotFoundException{
		try {
			return ofy.get(Subscription.getKey(id));
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("No Subscription package found with id={}", id);
			throw new NotFoundException("No package found with id: " + id);
		}
	}
	
	/**
	 * Update a subscription package or a specific subscription.
	 * 
	 * @param subscription
	 * @param subscriptionData
	 * @return Updated entity.
	 */
	public Subscription update(Subscription subscription, SubscriptionDTO subscriptionData) {
		checkNotNull(subscription, "subscription was null");
		checkNotNull(subscriptionData, "subscriptionData was null");
		
		validator.validate(subscriptionData);
		
		boolean subscriptionWasBasic = subscription.isBasic();
		
		if(!subscription.isBasic()) {
			subscription.setBasic(subscriptionData.isBasic());
		}
		
		subscription.setEndData(subscriptionData.getEndDate());
		subscription.setFee(subscriptionData.getFeeMinor());
		subscription.setMaxSpotCount(subscriptionData.getMaxSpotCount());
		subscription.setName(subscriptionData.getName());
		subscription.setStartDate(subscriptionData.getStartDate());
		subscription.setStatus(subscriptionData.getStatus());
		
		if(subscription.isTemplate() && !subscriptionWasBasic && subscription.isBasic()) {
			logger.info(
					"Querying for current basic subscription...");
			Subscription currentBasicSubscription = ofy.query(Subscription.class).filter("template", true).filter("basic", true).get();

			if (currentBasicSubscription != null) {
				logger.info(
						"Found basic subscription (id={}). Set Subscription({}) as new basic subscription.",currentBasicSubscription.getId(),
						subscription.getId());
				
				currentBasicSubscription.setBasic(false);
		
				ofy.put(currentBasicSubscription, subscription);
			}
			else {
				// Save normally
				logger.info("No basic subscription found. Saving as new basic.");
				ofy.put(subscription);
			}
		}
		else {
			// Save normally
			ofy.put(subscription);
		}
		
		return subscription;
	}
	
	/**
	 * Permanently deletes a subscription package from the datastore.
	 * 
	 * @param name unique name for the package
	 */
	public void deletePackage(long id) {
		ofy.delete(Subscription.class, id);
	}
	
	public Subscription createSubscriptionFromTemplate(Subscription template, SubscriptionStatus status, Long businessId, boolean saveBusiness) {
		return createSubscriptionFromTemplate(template, status, ofy.get(Business.class, businessId), saveBusiness);
	}
	
	public Subscription createSubscriptionFromTemplate(Subscription template, SubscriptionStatus status, Business business, boolean saveBusiness) {
		checkNotNull(template, "subscription template was null");
		checkNotNull(status, "status was null");
		checkNotNull(business, "business was null");
		
		Subscription newSubscription = new Subscription();
		
		if(status == SubscriptionStatus.APPROVED) {
			Subscription activeSubscription;
			
			if(business.getActiveSubscription() != null) {
				activeSubscription = ofy.get(business.getActiveSubscription());
				activeSubscription.setEndData(new Date());
				activeSubscription.setStatus(SubscriptionStatus.ARCHIVED);
				ofy.put(activeSubscription);
			}
			
			newSubscription.setStartDate(new Date());
		}
		
		newSubscription.setStatus(status);
		newSubscription.setBasic(template.isBasic());
		newSubscription.setBusiness(business.getKey());
		newSubscription.setFee(template.getFee());
		newSubscription.setMaxSpotCount(template.getMaxSpotCount());
		newSubscription.setName(template.getName());
		newSubscription.setTemplate(false);
		newSubscription.setTemplateKey(template.getKey());
		
		Key<Subscription> subscriptionKey = ofy.put(newSubscription);
		
		if(status == SubscriptionStatus.APPROVED) {
			business.setActiveSubscription(subscriptionKey);
		}
		
		return newSubscription;
	}
	
	@Subscribe
	public void handleNewBusinessEvent(NewLocationEvent event) {
		Subscription basicSubscription = ofy.query(Subscription.class).filter("template", true).filter("basic", true).get();
		
		if(basicSubscription == null) {
			logger.warn("No Subscription flagged as basic found for setting at new business");
		}
		else {
			Subscription newSubscription = createSubscriptionFromTemplate(basicSubscription, SubscriptionStatus.APPROVED, event.getLocation(), false);
		}
	}
	
	/**
	 * @param business
	 * @return
	 */
	public Subscription getActiveSubscription(Business business) {
		if(business.getActiveSubscription() == null) {
			return null;
		}
		
		return ofy.find(business.getActiveSubscription());
	}
}
