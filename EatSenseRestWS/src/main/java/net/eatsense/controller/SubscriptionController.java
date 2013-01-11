package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.event.DeleteSpotEvent;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.event.NewPendingSubscription;
import net.eatsense.event.NewSpotEvent;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.ObjectifyKeyFactory;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.TemplateChecks;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

public class SubscriptionController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Objectify ofy;
	private final ObjectifyKeyFactory ofyKeys;
	private final ValidationHelper validator;
	private final Provider<Subscription> subscriptionProvider;
	private final EventBus eventBus;

	@Inject
	public SubscriptionController(OfyService ofy,  ValidationHelper validator, Provider<Subscription> subscriptionProvider, EventBus eventBus) {
		super();
		this.subscriptionProvider = subscriptionProvider;
		this.ofy = ofy.ofy();
		this.ofyKeys = ofy.keys();
		this.eventBus = eventBus;
		this.validator = validator;
	}
	
	public Subscription createAndSaveTemplate(SubscriptionDTO subscriptionData) {
		Subscription subscription = subscriptionProvider.get();
		subscription.setTemplate(true);
		subscriptionData.setStatus(null);
		return updateTemplate(subscription, subscriptionData);	
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
			return ofy.get(Subscription.class, id);
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
	 * Shortcut method.
	 * Load and update a current Subscription entity from a Business.
	 * 
	 * @param locationId
	 * @param subscriptionId
	 * @param subscriptionData
	 * @return updated Subscription entity
	 */
	public Subscription getAndUpdateSubcription(long locationId,
			long subscriptionId, SubscriptionDTO subscriptionData) {
		return updateSubscription(get(locationId, subscriptionId), subscriptionData);
	}
	
	/**
	 * Update a Subscription template entity
	 * 
	 * @param subscription
	 * @param subscriptionData
	 * @return
	 */
	public Subscription updateSubscription(Subscription subscription, SubscriptionDTO subscriptionData) {
		checkNotNull(subscription, "subscription was null");
		checkNotNull(subscriptionData, "subscriptionData was null");
		
		validator.validate(subscriptionData);
		
		if(subscription.getStatus() == SubscriptionStatus.PENDING && subscriptionData.getStatus() == SubscriptionStatus.APPROVED) {
			subscription.setStartDate(new Date());
			Business location = ofy.get(subscription.getBusiness());
			location.setPendingSubscription(null);
			setActiveSubscription(location, Optional.of(subscription), true);
		}
		else if (subscription.getStatus() == SubscriptionStatus.PENDING && subscriptionData.getStatus() == SubscriptionStatus.CANCELED) {
			Business location = ofy.get(subscription.getBusiness());
			cancelPendingSubscription(location, Optional.of(subscription), true, false);
		}
		else if(subscription.getStatus() == SubscriptionStatus.APPROVED && subscriptionData.getStatus() == SubscriptionStatus.ARCHIVED) {
			Business location = ofy.get(subscription.getBusiness());
			setBasicSubscription(location);
		}
		
		subscription.setFee(subscriptionData.getFeeMinor());
		subscription.setMaxSpotCount(subscriptionData.getMaxSpotCount());
		subscription.setName(subscriptionData.getName());
		subscription.setStatus(subscriptionData.getStatus());
		
		ofy.put(subscription);
		
		return subscription;
	}
	
	/**
	 * Update a Subscription template entity
	 * 
	 * @param subscription
	 * @param subscriptionData
	 * @return Updated entity.
	 */
	public Subscription updateTemplate(Subscription subscription, SubscriptionDTO subscriptionData) {
		checkNotNull(subscription, "subscription was null");
		checkNotNull(subscriptionData, "subscriptionData was null");
		
		if(!subscription.isTemplate()) {
			throw new ValidationException("Subscription is not a template");
			
		}
		validator.validate(subscriptionData, TemplateChecks.class);
		
		boolean subscriptionWasBasic = subscription.isBasic();
		
		if(!subscriptionWasBasic) {
			subscription.setBasic(subscriptionData.isBasic());
		}
		
		subscription.setEndDate(subscriptionData.getEndDate());
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
	public void deleteTemplate(long id) {
		ofy.delete(Subscription.class, id);
	}
	
	/**
	 * Create and save new Subscription entity for a Business from a template.
	 * 
	 * @param templateId
	 * @param status
	 * @param businessId
	 * @return new {@link Subscription} entity
	 */
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
		
		Key<Business> locationKey = ofyKeys.create(Business.class, businessId);
		Subscription newSubscription = createSubscriptionFromTemplate(getTemplate(templateId), status, locationKey);
		
		Business location = ofy.get(locationKey);
		
		if(status == SubscriptionStatus.PENDING) {
			setPendingSubscription(location, newSubscription, true);
			eventBus.post(new NewPendingSubscription(newSubscription, location));
		}
		else if(status == SubscriptionStatus.APPROVED){
			setActiveSubscription(location, Optional.of(newSubscription), false);
			cancelPendingSubscription(location, Optional.<Subscription>absent(), true, true);
		}
		
		return newSubscription;
	}
	
	public void removePendingSubscription(Business location, long subscriptionId) {
		checkNotNull(location, "location was null");
		
		if(location.getPendingSubscription() != null) {
			if(location.getPendingSubscription().getId() == subscriptionId) {
				ofy.async().delete(location.getPendingSubscription());
				location.setPendingSubscription(null);
				ofy.put(location);
			}
			else {
				throw new NotFoundException();
			}
		}
		else {
			throw new NotFoundException();
		}
	}
	
	private void cancelPendingSubscription(Business location, Optional<Subscription> pendingSubscriptionOpt, boolean saveLocation, boolean saveSubscription) {
		checkNotNull(location, "location was null");
		
		if(location.getPendingSubscription() != null) {
			Subscription pendingSubscription;
			if(pendingSubscriptionOpt.isPresent())
				pendingSubscription = pendingSubscriptionOpt.get();
			else
				pendingSubscription = ofy.get(location.getPendingSubscription());
			
			pendingSubscription.setStatus(SubscriptionStatus.CANCELED);
			location.setPendingSubscription(null);
			
			if(saveSubscription) {
				ofy.async().put(pendingSubscription);
			}
		}
		if(saveLocation) {
			ofy.put(location);
		}
	}
	
	public Subscription createSubscriptionFromTemplate(Subscription template, SubscriptionStatus status,  Key<Business> businessKey) {
		checkNotNull(template, "subscription template was null");
		checkNotNull(status, "status was null");
		checkNotNull(businessKey, "businessKey was null");
		
		Subscription newSubscription = subscriptionProvider.get();
		
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
	
	public Business setActiveSubscription(Business location, Optional<Subscription> newSubscription, boolean saveBusiness) {
		checkNotNull(location, "business was null");
		checkNotNull(newSubscription, "newSubcription was null");
		
		if(location.getActiveSubscription() != null) {
			Subscription activeSubscription = ofy.find(location.getActiveSubscription());
			
			if(activeSubscription != null) {
				activeSubscription.setEndDate(new Date());
				activeSubscription.setStatus(SubscriptionStatus.ARCHIVED);
				ofy.async().put(activeSubscription);
			}
			else {
				logger.warn("Corrupt Location data, unable to find previous activeSubscription.");
			}
		}
		

		if(newSubscription.isPresent()) {
			Key<Subscription> newSubscriptionKey = newSubscription.isPresent() ? newSubscription.get().getKey() : null;
			// Reset Basic flag on business to the correct state
			location.setBasic(newSubscription.get().isBasic());
			location.setActiveSubscription(newSubscriptionKey);
			if(location.getSpotCount() == null) {
				location.setSpotCount(countSpots(location.getKey()));
			}
			recalculateQuota(location, newSubscription, location.getSpotCount(), false);
		}
		else {
			location.setActiveSubscription(null);
		}
		
		if(saveBusiness) {
			ofy.put(location);
		}
		
		return location;
	}
	
	public Business setPendingSubscription(Business location, Subscription newSubscription, boolean saveBusiness) {
		checkNotNull(location, "business was null");
		checkNotNull(newSubscription, "newSubcription was null");
		
		if(location.getPendingSubscription() != null) {
			ofy.async().delete(location.getPendingSubscription());
		}
				
		location.setPendingSubscription(newSubscription.getKey());
		
		if(saveBusiness) {
			ofy.put(location);
		}
		
		return location;
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
	
	public Subscription setBasicSubscription(Business location) {
		Subscription basicSubscription = ofy.query(Subscription.class).filter("template", true).filter("basic", true).get();
		
		if(basicSubscription == null) {
			logger.warn("No Subscription flagged as basic found, unable to set active subscription for new Location.");
			setActiveSubscription(location, Optional.<Subscription>absent(), true);
		}
		else {
			Subscription newSubscription = createSubscriptionFromTemplate(basicSubscription, SubscriptionStatus.APPROVED, location.getKey());
			
			setActiveSubscription(location, Optional.of(newSubscription), true);
		}
		
		return basicSubscription;
	}

	/**
	 * @param locationKey
	 * @param newSpotCount
	 */
	private void recalculateQuota(Business location, Optional<Subscription> activeSubOpt, int newSpotCount, boolean save) {
		
		logger.info("newSpotCount={}", newSpotCount);
		
		if(location == null) {
			logger.error("No Location supplied for quota calculation");
			return;
		}

		if(location.isBasic() || location.getActiveSubscription() == null) {
			logger.info("Skipped quota check for basic business.");
			return;
		}
		Subscription activeSub;
		if(activeSubOpt.isPresent()) {
			activeSub = activeSubOpt.get();
		}
		else {
			activeSub = ofy.find(location.getActiveSubscription());
		}
		 
		if(activeSub == null) {
			logger.error("Corrupt Location data, activeSubscription not found");
			return;
		}
		
		if(activeSub.getMaxSpotCount() > 0) {
			logger.info("newSpotCount={}, maxSpotCount={}", newSpotCount, activeSub.getMaxSpotCount());
			if(activeSub.getMaxSpotCount() < newSpotCount) {
				logger.warn("Quota exceeded for Location. name={}, id={}", location.getName(), location.getId());
				
				activeSub.setQuotaExceeded(true);
				ofy.async().put(activeSub);
				//TODO Send email for the first time the quota was exceeded
			}
			else {
				logger.info("Quota no longer exceeded for Location. name={}, id={}", location.getName(), location.getId());
				activeSub.setQuotaExceeded(false);
				ofy.async().put(activeSub);
			}
		}	
	}
	
	/**
	 * Handles basic Subscription creation before creation of a new Business in the store.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleNewLocationEvent(NewLocationEvent event) {
		logger.info("Adding basic subscription ...");
		setBasicSubscription(event.getLocation());
	}
	
	@Subscribe
	public void handleNewSpotEvent(NewSpotEvent event) {
		if(event.getLocation() == null) {
			logger.error("Corrupt event received, locationKey was null");
			return;
		}
		
		recalculateQuota(ofy.find(event.getLocation()),Optional.<Subscription>absent(), event.getNewSpotCount(), true);
	}

	@Subscribe
	public void handleDeleteSpotEvent(DeleteSpotEvent event) {
		if(event.getLocation() == null) {
			logger.error("Corrupt event received, locationKey was null");
			return;
		}
		
		recalculateQuota(ofy.find(event.getLocation()),Optional.<Subscription>absent(), event.getNewSpotCount(), true);
	}
	
	/**
	 * @param locationKey
	 * @return
	 */
	private int countSpots(Key<Business> locationKey) {
		return ofy.query(Spot.class).ancestor(locationKey).filter("trash", false).count();
	}
}
