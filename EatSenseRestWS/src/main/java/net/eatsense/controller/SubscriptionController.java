package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;
import net.eatsense.domain.Subscription;
import net.eatsense.representation.SubscriptionDTO;
import net.eatsense.validation.ValidationHelper;

import com.googlecode.objectify.Objectify;

public class SubscriptionController {
	private final Objectify ofy;
	private final ValidationHelper validator;

	public SubscriptionController(Objectify ofy,  ValidationHelper validator) {
		super();
		
		this.ofy = ofy;
		this.validator = validator;
	}
	
	public Subscription createAndSavePackage(SubscriptionDTO subscriptionData) {
		Subscription subscription = new Subscription();
		
		return update(subscription, subscriptionData);	
	}
	
	public Iterable<Subscription> getAllPackages() {
		return ofy.query(Subscription.class).filter("business", null).fetch();
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
		subscription.setMaxSpotCount(subscription.getMaxSpotCount());
		subscription.setName(subscription.getName());
		subscription.setStartDate(subscriptionData.getStartDate());
		subscription.setStatus(subscriptionData.getStatus());
		
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
