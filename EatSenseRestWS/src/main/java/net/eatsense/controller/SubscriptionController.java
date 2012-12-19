package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import javax.validation.groups.Default;

import net.eatsense.domain.Business;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.TemplateChecks;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

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
	
	public Subscription createAndSaveTemplate(SubscriptionDTO subscriptionData) {
		Subscription subscription = new Subscription();
		subscription.setTemplate(true);
		subscriptionData.setStatus(null);
		return update(subscription, subscriptionData);	
	}
	
	/**
	 * Shortcut method for {@link #get(boolean, long, SubscriptionStatus)}
	 * @param businessId
	 * @return All Subscriptions for this entity.
	 */
	public Iterable<Subscription> get(long businessId) {
		return get(false, businessId, null);
	}
	
	/**
	 * @param businessId
	 * @return All Subscriptions for this entity.
	 */
	public Iterable<Subscription> get(boolean isTemplate, long businessId, SubscriptionStatus status) {
		Query<Subscription> query = ofy.query(Subscription.class);
		
		if(isTemplate) {
			query = query.filter("template", isTemplate);
		}
		else if(businessId != 0) {
			query = query.ancestor(Business.getKey(businessId));
		}
		
		if(status != null) {
			query = query.filter("status", status);
		}
		
		return query;
	}
	
	/**
	 * @param name
	 * @return Subscription entity template saved with that id
	 */
	public Subscription getTemplate(long id) throws NotFoundException{
		try {
			return ofy.get(Subscription.getKey(id));
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("No Subscription package found with id={}", id);
			throw new NotFoundException("No package found with id: " + id);
		}
	}
	
	/**
	 * @param businessId
	 * @param subscriptionId
	 * @return Subscription entity
	 */
	public Subscription get(long businessId, long subscriptionId) {
		try {
			return ofy.get(Subscription.getKey(businessId, subscriptionId));
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
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
		
		if(subscription.isTemplate()) {
			validator.validate(subscriptionData, TemplateChecks.class);
		}
		else {
			validator.validate(subscriptionData, Default.class);
		}
		
		
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
	
	public Subscription createAndSetSubscription(Long templateId, SubscriptionStatus status,  Long businessId) {
		if(templateId == null || templateId.longValue() == 0) {
			throw new ValidationException("templateId can't be null or 0");
		}
		if(status == null) {
			throw new ValidationException("status can't be null");
		}
		if(businessId == null || businessId.longValue() == 0) {
			throw new ValidationException("businessId can't be null or 0");
		}
		
		Subscription newSubscription = createSubscriptionFromTemplate(getTemplate(templateId), status, Business.getKey(businessId));
		
		Business location = ofy.get(Business.getKey(businessId));
		
		if(status == SubscriptionStatus.PENDING) {
			setPendingSubscription(location, newSubscription, true);
		}
		else if(status == SubscriptionStatus.APPROVED){
			setActiveSubscription(location, newSubscription, true);
		}
		
		return newSubscription;
	}
	
	public void removePendingSubscription(Business location, long subscriptionId) {
		checkNotNull(location, "location was null");
		
		if(location.getPendingSubscription() != null) {
			if(location.getPendingSubscription().getId() == subscriptionId) {
				ofy.delete(location.getPendingSubscription());
				location.setPendingSubscription(null);
			}
			else {
				throw new NotFoundException();
			}
		}
		else {
			throw new NotFoundException();
		}
		
		
	}
	
	public Subscription createSubscriptionFromTemplate(Subscription template, SubscriptionStatus status,  Key<Business> businessKey) {
		checkNotNull(template, "subscription template was null");
		checkNotNull(status, "status was null");
		checkNotNull(businessKey, "businessId was null");
		
		Subscription newSubscription = new Subscription();
		
		if(status == SubscriptionStatus.APPROVED) {
			newSubscription.setStartDate(new Date());
		}
		
		newSubscription.setStatus(status);
		newSubscription.setBasic(template.isBasic());
		newSubscription.setBusiness(businessKey);
		newSubscription.setFee(template.getFee());
		newSubscription.setMaxSpotCount(template.getMaxSpotCount());
		newSubscription.setName(template.getName());
		newSubscription.setTemplate(false);
		newSubscription.setTemplateKey(template.getKey());
		
		ofy.put(newSubscription);
		
		return newSubscription;
	}
	
	public Business setActiveSubscription(Business location, Subscription newSubscription, boolean saveBusiness) {
		checkNotNull(location, "business was null");
		checkNotNull(newSubscription, "newSubcription was null");
		
		if(location.getActiveSubscription() != null) {
			Subscription activeSubscription = ofy.find(location.getActiveSubscription());
			
			if(activeSubscription != null) {
				activeSubscription.setEndData(new Date());
				activeSubscription.setStatus(SubscriptionStatus.ARCHIVED);
				ofy.put(activeSubscription);
			}
			else {
				logger.warn("Corrupt Location data, unable to find previous activeSubscription.");
			}
		}
				
		location.setActiveSubscription(newSubscription.getKey());
		
		if(saveBusiness) {
			ofy.put(location);
		}
		
		return location;
	}
	
	public Business setPendingSubscription(Business location, Subscription newSubscription, boolean saveBusiness) {
		checkNotNull(location, "business was null");
		checkNotNull(newSubscription, "newSubcription was null");
		
		if(location.getPendingSubscription() != null) {
			ofy.delete(location.getPendingSubscription());
		}
				
		location.setPendingSubscription(newSubscription.getKey());
		
		if(saveBusiness) {
			ofy.put(location);
		}
		
		return location;
	}
	
	@Subscribe
	public void handleNewLocationEvent(NewLocationEvent event) {
		Subscription basicSubscription = ofy.query(Subscription.class).filter("template", true).filter("basic", true).get();
		
		if(basicSubscription == null) {
			logger.warn("No Subscription flagged as basic found, unable to set active subscription for new Location.");
		}
		else {
			Subscription newSubscription = createSubscriptionFromTemplate(basicSubscription, SubscriptionStatus.APPROVED, event.getLocation().getKey());
			
			setActiveSubscription(event.getLocation(), newSubscription, false);
		}
	}
	
	/**
	 * @param business
	 * @return active Subcription entity if found, <code>null</code> otherwise
	 */
	public Subscription getActiveSubscription(Business business) {
		if(business.getActiveSubscription() == null) {
			return null;
		}
		
		return ofy.find(business.getActiveSubscription());
	}
}
