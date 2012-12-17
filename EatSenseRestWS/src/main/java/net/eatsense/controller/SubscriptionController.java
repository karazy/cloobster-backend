package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.Subscription;
import net.eatsense.domain.embedded.SubscriptionStatus;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.OfyService;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.ValidationHelper;

import com.google.inject.Inject;
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
	 * @return All saved Subscription packages
	 */
	public Iterable<Subscription> getAll(boolean template) {
		return ofy.query(Subscription.class).filter("template", template).fetch();
	}
	
	/**
	 * @param name
	 * @return Subscription entity saved with that name
	 */
	public Subscription getPackage(String name) throws NotFoundException{
		try {
			return ofy.get(Subscription.getKey(name));
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("No Subscription package found with name={}", name);
			throw new NotFoundException("No package found with name: " + name);
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
		
		subscription.setBasic(subscriptionData.isBasic());
		subscription.setEndData(subscriptionData.getEndDate());
		subscription.setFee(subscriptionData.getFeeMinor());
		subscription.setMaxSpotCount(subscriptionData.getMaxSpotCount());
		subscription.setName(subscriptionData.getName());
		subscription.setStartDate(subscriptionData.getStartDate());
		subscription.setStatus(subscriptionData.getStatus());
		
		Subscription currentBasicSubscription = ofy.query(Subscription.class).filter("basic", true).get();
		if(subscriptionData.isBasic()) {
			if(currentBasicSubscription != null) {
				logger.info("Setting subscription \"{}\" as new basic subscription.", subscriptionData.getName());
				currentBasicSubscription.setBasic(false);
				ofy.async().put(currentBasicSubscription);
			}
		}
		
		ofy.put(subscription);
		
		return subscription;
	}
	
	/**
	 * Permanently deletes a subscription package from the datastore.
	 * 
	 * @param name unique name for the package
	 */
	public void deletePackage(String name) {
		ofy.delete(Subscription.class, name);
	}
}
